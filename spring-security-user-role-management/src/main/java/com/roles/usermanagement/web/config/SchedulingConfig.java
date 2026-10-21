package com.roles.usermanagement.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// [Nueva Funcionalidad: T-18/A-12 - Habilitación de tareas programadas] - Habilita jobs periódicos de monitoreo SLA y notificaciones en segundo plano - Conecta con: SlaBreachScheduler
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
