/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import dao.BookDAO;
import dao.OrderDAO;
import dao.OrderDetailDAO;
import entity.Book;
import entity.Cart;
import entity.Customer;
import entity.Order;
import entity.OrderDetail;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author BLC
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/CartServlet"})
public class CartServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try ( PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            BookDAO myBookDAO = new BookDAO();
            OrderDAO myOrderDAO = new OrderDAO();
            OrderDetailDAO myOrderDetailDAO = new OrderDetailDAO();
            String mode = request.getParameter("mode");
            String target = "Cart.jsp";
            HttpSession session = request.getSession();

            if (mode.equals("addToCart")) {
                ArrayList<Cart> listCart = (ArrayList<Cart>) session.getAttribute("listCart");
                int bookID = Integer.parseInt(request.getParameter("bookID"));
                Book book = myBookDAO.getBookByID(bookID);
                if (listCart == null) {
                    listCart = new ArrayList<>();
                }
                Cart cart = new Cart(bookID, book.getTitle(), book.getPrice(), 1);
                listCart.add(cart);
                session.setAttribute("listCart", listCart);
                target = "Cart.jsp";

                RequestDispatcher rd = request.getRequestDispatcher(target);
                rd.forward(request, response);
            }

            if (mode.equals("checkout")) {
                ArrayList<Cart> listOrder = (ArrayList<Cart>) session.getAttribute("listCart");
                Customer customer = (Customer) session.getAttribute("tempCustomer");

//                String pattern = "yyyy-MM-dd";
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String date = java.time.LocalDate.now().toString();

                Order newOrder = new Order(customer.getCustomer_id(), date, 15000, "pending", 1);

                int orderID = myOrderDAO.saveOrders(newOrder);
                if (orderID != 0) {
                    for (Cart cart : listOrder) {
                        Book book = myBookDAO.getBookByID(cart.getBookID());

                        int quantityOfBooks = book.getQuantity();
                        int quantityOfBuy = cart.getQuantity();

                        if (quantityOfBooks > 0 && quantityOfBuy < quantityOfBooks) {
                            OrderDetail orderDetail = new OrderDetail(orderID, cart.getBookID(), quantityOfBuy, book.getPrice());
                            myOrderDetailDAO.insertOrderDetail(orderDetail);

                            int restQuantity = quantityOfBooks - quantityOfBuy;
                            book.setQuantity(restQuantity);

                            myBookDAO.updateBook(book);
                            request.setAttribute("message", "Payment Success");
                            session.removeAttribute("listCart");
                            request.getRequestDispatcher("OrderSuccess.jsp").forward(request, response);
                        } else {
                            request.setAttribute("message", "Out Of Stock!");
                            request.getRequestDispatcher("OrderSuccess.jsp").forward(request, response);
                        }
                    }
                }

            }
//            RequestDispatcher rd = request.getRequestDispatcher(target);
//            rd.forward(request, response);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}