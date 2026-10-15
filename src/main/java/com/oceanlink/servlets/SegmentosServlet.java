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

@WebServlet(name = "SegmentosServlet", value = "/SegmentosServlet")

public class SegmentosServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action")==null ? "lista":request.getParameter("action");
        SegmentosDao segmentosDao = new SegmentosDao();
        Landing_StationsDao landing_StationsDao = new Landing_StationsDao();
        RequestDispatcher view;

        switch (action){
            case "lista":
                ArrayList<Segmentos> listaSegmentos = segmentosDao.obtenerListaSegmentos();
                request.setAttribute("listaSegmentos", listaSegmentos);

                view = request.getRequestDispatcher("listaSegmentos.jsp");
                view.forward(request, response);
                break;
            case "formCrear":
                request.setAttribute("listaLanding_Stations", landing_StationsDao.obtenerListaLanding_Stations());
                view = request.getRequestDispatcher("nuevoSegmentos.jsp");
                view.forward(request, response);
                break;
            case "editar":
                String Strg = request.getParameter("id");
                int idSegmento = Integer.parseInt(Strg);
                Segmentos segmentos =segmentosDao.obtenerSegmentos(idSegmento);
                if(segmentos==null){
                    response.sendRedirect(request.getContextPath()+"/EstacionesServlet");
                }else {
                    request.setAttribute("segmentos",segmentos);
                    request.setAttribute("listaLanding_Stations", landing_StationsDao.obtenerListaLanding_Stations());
                    view = request.getRequestDispatcher("editarSegmentos.jsp");
                    view.forward(request, response);
                }
                break;
            case "borrar":
                String idStr = request.getParameter("id");
                idSegmento = Integer.parseInt(idStr);
                if (segmentosDao.obtenerSegmentos(idSegmento) != null) {
                    segmentosDao.borrarSegmentos(idSegmento);
                }
                response.sendRedirect(request.getContextPath() + "/SegmentosServlet");
                break;
        }




    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action") ==null ? "lista":request.getParameter("action");
        SegmentosDao segmentosDao = new SegmentosDao();
        RequestDispatcher view;

        switch (action){
            case "crear":

                String idSegmentoStr = request.getParameter("id");
                String etiqueta = request.getParameter("etiquetaSegmento");
                String estacionOrigen = request.getParameter("estacionOrigen");
                String estacionDestino = request.getParameter("estacionDestino");
                String capacidadTotal =  request.getParameter("capacidadTotal");

                int idSegmento = (idSegmentoStr != null && !idSegmentoStr.isEmpty()) ? Integer.parseInt(idSegmentoStr) : 0;

                Segmentos segmento = segmentosDao.obtenerSegmentos(idSegmento);

                if(segmento == null){
                    segmentosDao.crearSegmentos(etiqueta, estacionOrigen, estacionDestino,capacidadTotal);
                } else {
                    segmentosDao.actualizarSegmentos(idSegmento, etiqueta,estacionOrigen,estacionDestino,capacidadTotal);
                }
                response.sendRedirect(request.getContextPath() + "/SegmentosServlet");
                break;

        }
    }
}
