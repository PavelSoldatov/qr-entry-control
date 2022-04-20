package ru.unit_techno.qr_entry_control_impl.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.unit.techno.ariss.barrier.api.dto.BarrierRequestDto;
import ru.unit.techno.ariss.log.action.lib.api.LogActionBuilder;
import ru.unit.techno.ariss.log.action.lib.entity.Description;
import ru.unit.techno.ariss.log.action.lib.model.ActionStatus;
import ru.unit.techno.device.registration.api.DeviceResource;
import ru.unit.techno.device.registration.api.dto.DeviceResponseDto;
import ru.unit.techno.device.registration.api.enums.DeviceType;
import ru.unit_techno.qr_entry_control_impl.entity.CardEntity;
import ru.unit_techno.qr_entry_control_impl.entity.QrCodeEntity;
import ru.unit_techno.qr_entry_control_impl.entity.enums.CardStatus;
import ru.unit_techno.qr_entry_control_impl.mapper.EntryDeviceToReqRespMapper;
import ru.unit_techno.qr_entry_control_impl.repository.CardRepository;
import ru.unit_techno.qr_entry_control_impl.repository.QrRepository;
import ru.unit_techno.qr_entry_control_impl.util.DateValidator;
import ru.unit_techno.qr_entry_control_impl.websocket.WSNotificationService;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final QrRepository qrRepository;
    private final CardRepository cardRepository;
    private final DeviceResource deviceResource;
    private final EntryDeviceToReqRespMapper reqRespMapper;
    private final LogActionBuilder logActionBuilder;
    private final WSNotificationService notificationService;
    private final BarrierFeignService barrierFeignService;

    @SneakyThrows
    @Transactional
    public void returnCard(String cardValue, Long deviceId) {
        Optional<CardEntity> issuedCard = cardRepository.findByCardValue(cardValue);
        CardEntity card = null;

        if (issuedCard.isPresent()) {
            card = issuedCard.get();
        }

        if (card != null && card.getCardStatus() == CardStatus.ISSUED) {
            card.setCardStatus(CardStatus.RETURNED);
        } else {
            throw new EntityNotFoundException("Данная карта не найдена");
        }

        CardEntity save = cardRepository.save(card);
        QrCodeEntity qrCodeEntity = qrRepository.findByCardId(save.getId());

        DateValidator.checkQrEnteringDate(qrCodeEntity);

        try {
            DeviceResponseDto entryDevice = deviceResource.getGroupDevices(deviceId, DeviceType.CARD);
            BarrierRequestDto barrierRequest = reqRespMapper.entryDeviceToRequest(entryDevice);
            barrierFeignService.openBarrier(barrierRequest, qrCodeEntity);

            //после присвоения null у qr, карточка удаляется автоматически
            qrCodeEntity.setCard(null);
            qrRepository.save(qrCodeEntity);

            logActionBuilder.buildActionObjectAndLogAction(barrierRequest.getBarrierId(),
                    qrCodeEntity.getQrId(),
                    qrCodeEntity.getGovernmentNumber(),
                    ActionStatus.UNKNOWN);
        } catch (Exception e) {
            /// TODO: 27.09.2021 Продумать возможные кейсы ошибок и эксепшенов, сделать обработки
            notificationService.sendCardNotReturned(qrCodeEntity.getGovernmentNumber(), deviceId);
            logActionBuilder.buildActionObjectAndLogAction(deviceId,
                    qrCodeEntity.getQrId(),
                    qrCodeEntity.getGovernmentNumber(),
                    ActionStatus.UNKNOWN,
                    true,
                    new Description()
                            .setErroredServiceName("QR")
                            .setMessage("Some problems while returning card in column. Error message: " + e.getMessage()));
            /// TODO: 20.12.2021 Нужно сделать отдельный рест вызов прошивки, чтобы вернуть карту в случае если шлагбаум не открылся RELEASE 1.0
        }
    }
}
