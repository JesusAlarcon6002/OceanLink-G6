package com.oceanlink.servlets;

import com.oceanlink.beans.Landing_Stations;
import com.oceanlink.beans.Segmentos;
import com.oceanlink.beans.Usuarios;
import com.oceanlink.daos.Landing_StationsDao;
import com.oceanlink.daos.SegmentosDao;
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
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action")==null ? "lista":request.getParameter("action");
        Landing_StationsDao landing_StationsDao = new Landing_StationsDao();
        RequestDispatcher view;

        switch (action){
            case "lista":
                ArrayList<Landing_Stations> listaLanding_Stations = landing_StationsDao.obtenerListaLanding_Stations();
                request.setAttribute("listaLanding_Stations", listaLanding_Stations);

                view = request.getRequestDispatcher("listaLanding_Stations.jsp");
                view.forward(request, response);
                break;
            case "formCrear":
                request.setAttribute("listaLanding_Stations", landing_StationsDao.obtenerListaLanding_Stations());
                view = request.getRequestDispatcher("nuevoLanding_Stations.jsp");
                view.forward(request, response);
                break;
            case "editar":
                String Strg = request.getParameter("id");
                int idLandingStation = Integer.parseInt(Strg);
                Landing_Stations landing_stations = new Landing_StationsDao().obtenerLandingStations(idLandingStation);
                if(landing_stations==null){
                    response.sendRedirect(request.getContextPath()+"/EstacionesServlet");
                }else {
                    request.setAttribute("landingStations",landing_stations);
                    view = request.getRequestDispatcher("editarLanding_Stations.jsp");
                    view.forward(request, response);
                }
                break;
            case "borrar":
                String idStr = request.getParameter("id");
                idLandingStation = Integer.parseInt(idStr);
                if (landing_StationsDao.obtenerLandingStations(idLandingStation) != null) {
                    landing_StationsDao.borrarLandingStations(idLandingStation);
                }
                response.sendRedirect(request.getContextPath() + "/EstacionesServlet");
                break;
        }
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action") ==null ? "lista":request.getParameter("action");
        Landing_StationsDao  landing_StationsDao = new Landing_StationsDao();
        RequestDispatcher view;

        switch (action){
            case "crear":

                String idLandingStationStr = request.getParameter("id");
                String etiqueta = request.getParameter("etiquetaLandingStation");
                String ubicacion = request.getParameter("ubicacion");


                int idLandingStation = (idLandingStationStr != null && !idLandingStationStr.isEmpty()) ? Integer.parseInt(idLandingStationStr) : 0;

                Landing_Stations landing_stations = new Landing_StationsDao().obtenerLandingStations(idLandingStation);

                if(landing_stations == null){
                    landing_StationsDao.crearLandingStations(etiqueta, ubicacion);
                } else {
                    landing_StationsDao.actualizarLandingStations(idLandingStation, etiqueta, ubicacion);
                }
                response.sendRedirect(request.getContextPath() + "/EstacionesServlet");
                break;

        }
    }
}
