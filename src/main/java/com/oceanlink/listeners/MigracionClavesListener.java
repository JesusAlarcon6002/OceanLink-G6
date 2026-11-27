package com.oceanlink.listeners;

import com.oceanlink.daos.MigracionClavesDao;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Al arrancar Tomcat, convierte a hash las contraseñas antiguas que sigan en texto plano.
 * Es automático e idempotente: si ya está todo hasheado no hace nada. Si la BD no responde solo avisa en consola.
 */
@WebListener
public class MigracionClavesListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        try {
            int n = new MigracionClavesDao().hashearPendientes();
            System.out.println("[Seguridad] Contraseñas convertidas a hash al arrancar: " + n);
        } catch (Exception e) {
            System.err.println("[Seguridad] No se pudo revisar las contraseñas al arrancar: " + e.getMessage());
        }
    }
}
