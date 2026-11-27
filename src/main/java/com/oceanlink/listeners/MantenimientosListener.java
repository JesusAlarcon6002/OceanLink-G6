package com.oceanlink.listeners;

import com.oceanlink.daos.MantenimientosDao;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Cada minuto mantiene sincronizados los mantenimientos con la infraestructura:
 *  1. Los mantenimientos Programados cuya fecha y hora ya llegaron pasan solos a "En curso".
 *  2. Todo segmento o estación con un mantenimiento "En curso" queda "En mantenimiento".
 * (Al finalizar un mantenimiento, MantenimientosServlet devuelve la infraestructura a "Operativo".)
 * Arranca 30 segundos después de levantar Tomcat.
 */
@WebListener
public class MantenimientosListener implements ServletContextListener {

    private ScheduledExecutorService reloj;

    /** Una pasada de sincronización (también la usan las pruebas). */
    public static void sincronizar() {
        MantenimientosDao dao = new MantenimientosDao();
        int iniciados = dao.iniciarVencidos();
        int marcados = dao.marcarInfraEnMantenimiento();
        if (iniciados > 0 || marcados > 0) {
            System.out.println("[Mantenimientos] " + iniciados + " mantenimiento(s) pasaron a En curso; "
                    + marcados + " segmento(s)/estacion(es) quedaron En mantenimiento");
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        reloj = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "oceanlink-mantenimientos");
            t.setDaemon(true);
            return t;
        });
        reloj.scheduleWithFixedDelay(() -> {
            try {
                sincronizar();
            } catch (Exception e) {
                System.err.println("[Mantenimientos] Falló la sincronización: " + e.getMessage());
            }
        }, 30, 60, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        if (reloj != null) {
            reloj.shutdownNow();
        }
    }
}
