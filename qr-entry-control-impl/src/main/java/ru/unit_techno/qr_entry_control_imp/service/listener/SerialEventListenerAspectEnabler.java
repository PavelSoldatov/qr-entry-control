package ru.unit_techno.qr_entry_control_imp.service.listener;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import ru.unit_techno.qr_entry_control_imp.config.SerialPortTemplate;

@Aspect
@Slf4j
public class SerialEventListenerAspectEnabler {

    private SerialPortTemplate serialPortTemplate;

    @Autowired
    public SerialEventListenerAspectEnabler(SerialPortTemplate serialPortTemplate) {
        this.serialPortTemplate = serialPortTemplate;
    }

    @AfterThrowing(value = "execution(public * ru.unit_techno.qr_entry_control_imp.service.*.*(..)) && @annotation(EventListenerCanCrash)", throwing = "e")
    private void activateSerialPortListener(JoinPoint joinPoint, Throwable e) {
        log.info("SerialPort exception occurred: {} \r\n Add new listener on port", e.getMessage());
        serialPortTemplate.enableEventListener();
    }
}