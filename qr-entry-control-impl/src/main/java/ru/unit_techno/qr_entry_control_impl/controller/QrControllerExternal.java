
package ru.unit_techno.qr_entry_control_impl.controller;

import lombok.RequiredArgsConstructor;
import net.kaczmarzyk.spring.data.jpa.domain.Between;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;
import ru.unit_techno.qr_entry_control_impl.dto.QrCodeDto;
import ru.unit_techno.qr_entry_control_impl.dto.QrInfoDto;
import ru.unit_techno.qr_entry_control_impl.entity.QrCodeEntity;
import ru.unit_techno.qr_entry_control_impl.service.QrService;

import javax.validation.Valid;

@RestController
@RequestMapping("/ui/qr")
@RequiredArgsConstructor
public class QrControllerExternal {

    private final QrService qrService;

    @PostMapping("/createAndSend")
    public Long createQrAndSend(@Valid @RequestBody QrCodeDto qrCodeDto) {
        return qrService.createAndSendQrToEmail(qrCodeDto);
    }

    @GetMapping("/allQrCodesInfo")
    public Page<QrInfoDto> getQrCodesInfo(
            @And({@Spec(path = "qrId", params = "qrId", spec = Equal.class),
                    @Spec(path = "governmentNumber", params = "governmentNumber", spec = Equal.class),
                    @Spec(path = "enteringDate", params = {"before", "after"}, spec = Between.class, config = "yyyy-MM-dd"),
                    @Spec(path = "fullName", params = "fullName", spec = Like.class)
            }) Specification<QrCodeEntity> specificationPageable, Pageable pageable) {
        return qrService.getAllQrCodesInfo(specificationPageable, pageable);
    }
}