package com.oceanlink.servlets;

import com.oceanlink.beans.Landing_Stations;
import com.oceanlink.beans.Segmentos;
import com.oceanlink.daos.Landing_StationsDao;
import com.oceanlink.daos.SegmentosDao;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "SegmentosServlet", value = "/SegmentosServlet")

public class SegmentosServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action")==null ? "lista":request.getParameter("action");
        SegmentosDao SegmentosDao = new SegmentosDao();
        Landing_StationsDao landing_StationsDao = new Landing_StationsDao();
        RequestDispatcher view;

        switch (action){
            case "lista":
                ArrayList<Segmentos> listaSegmentos = SegmentosDao.obtenerListaSegmentos();
                request.setAttribute("listaSegmentos", listaSegmentos);

                view = request.getRequestDispatcher("listaSegmentos.jsp");
                view.forward(request, response);
                break;
            case "formCrear":
                request.setAttribute("listaLanding_Stations", landing_StationsDao.obtenerListaLanding_Stations());
                view = request.getRequestDispatcher("nuevoSegmentos.jsp");
                view.forward(request, response);
                break;
        }




    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
