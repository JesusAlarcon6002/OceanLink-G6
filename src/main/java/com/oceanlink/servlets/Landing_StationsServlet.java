package com.oceanlink.servlets;

import com.oceanlink.beans.Landing_Stations;
import com.oceanlink.beans.Usuarios;
import com.oceanlink.daos.Landing_StationsDao;
import com.oceanlink.daos.UsuariosDao;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "Landing_StationsServlet", value = "/EstacionesServlet")

public class Landing_StationsServlet  extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Landing_StationsDao Landing_StationsDao = new Landing_StationsDao();
        ArrayList<Landing_Stations> listaLanding_Stations = Landing_StationsDao.obtenerListaLanding_Stations();

        request.setAttribute("listaLanding_Stations", listaLanding_Stations);

        RequestDispatcher view = request.getRequestDispatcher("listaLanding_Stations.jsp");
        view.forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{

    }
}
