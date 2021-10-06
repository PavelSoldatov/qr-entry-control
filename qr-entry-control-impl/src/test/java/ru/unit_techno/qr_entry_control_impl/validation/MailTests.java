package ru.unit_techno.qr_entry_control_impl.validation;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import ru.unit_techno.qr_entry_control_impl.base.BaseTestClass;
import ru.unit_techno.qr_entry_control_impl.dto.QrCodeDto;

import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;

public class MailTests extends BaseTestClass {
    String BASE_URL = "/ui/qr";

    @MockBean
    JavaMailSender javaMailSender;

    /*
        1. кидаем на рест запрос с созданием и отправкой qr
        2. мокаем javaMailSender, чтобы он вернул эксепшен
        3.Проверяем что в мапе лежит сообщение на переотправку
        4. делаем Mockito.reset на java mail sender
        5.Принудительно вызываем переотправку, проверяем что сообщения на переотправку в мапе нет, в базе статус деливери, письмо дошло на грин меил
     */
    @Test
    void shouldSendEmailWithCorrectPayloadToUser() throws Exception {
        Mockito.doThrow(new MailException("sadfas") {}).when(javaMailSender).send(any(MimeMessage.class));

        QrCodeDto qrCodeDto = new QrCodeDto()
                .setEmail("pavsoldatov96@gmail.com")
                .setName("aa")
                .setSurname("bb")
                .setGovernmentNumber("А777АА 77")
                .setEnteringDate(LocalDateTime.of(2021, 11, 11, 11, 11, 11));

        String resultUrl = BASE_URL + "/createAndSend";

        testUtils.invokePostApi(Long.class, resultUrl, HttpStatus.CONFLICT, qrCodeDto);
    }
}