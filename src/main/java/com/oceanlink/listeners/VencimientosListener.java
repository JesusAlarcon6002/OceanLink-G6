package com.oceanlink.listeners;

import com.oceanlink.services.NotificadorCorreos;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Cada 24 horas revisa los servicios Activos que vencen en 30 o en 7 días y avisa al cliente (y un resumen al personal).
 * Arranca 1 minuto después de levantar Tomcat. Para no repetir avisos mientras Tomcat sigue encendido se recuerda,
 * en memoria, qué servicio ya se avisó (si se reinicia Tomcat el mismo día, podría repetirse: no usa tablas nuevas).
 */
@WebListener
public class VencimientosListener implements ServletContextListener {

    public static final int[] DIAS_AVISO = {30, 7};
    private static final Set<String> YA_AVISADOS = ConcurrentHashMap.newKeySet();
    private ScheduledExecutorService reloj;

    /** @param automatico false = lo pidió el admin con el botón: se vuelve a avisar aunque ya se haya hecho hoy. */
    public static synchronized int revisar(boolean automatico) {
        if (!automatico) {
            YA_AVISADOS.clear();
        }
        int total = 0;
        for (int dias : DIAS_AVISO) {
            total += NotificadorCorreos.serviciosPorVencer(dias, YA_AVISADOS);
        }
        System.out.println("[Correo] Revisión de servicios por vencer: " + total + " servicio(s) avisado(s)");
        return total;
    }

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        reloj = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "oceanlink-vencimientos");
            t.setDaemon(true);
            return t;
        });
        reloj.scheduleAtFixedRate(() -> {
            try {
                revisar(true);
            } catch (Exception e) {
                System.err.println("[Correo] Falló la revisión de vencimientos: " + e.getMessage());
            }
        }, 1, 24 * 60, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        if (reloj != null) {
            reloj.shutdownNow();
        }
    }
}
