package controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.Logic;
import model.beans.UserBean;

/**
 * Servlet implementation class UserBean
 */
@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String alias;
	private String clave;
	private UserBean isLogin;
	private Logic logic;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Login() {
		super();
		logic = new Logic();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");

		HttpSession session = request.getSession();

		// Obtenemos el alias y la clave.
		alias = request.getParameter("alias");
		clave = request.getParameter("clave");

		if (request.getParameter("login") != null) {
			// En primer lugar comprobamos que no hay un usuario logeado con la
			// variable de aplcación.
			isLogin = (UserBean) getServletContext().getAttribute("isLogin");
			if (isLogin == null) {
				boolean admin = false;
				boolean usuario = false;
				// Comprobamos si el usuario existe
				UserBean user = logic.verificaUsuario(alias, clave);
				if (user != null) {
					user.setSessionId(session.getId());
					getServletContext().setAttribute("isLogin", user);
					usuario = true;
					// Comprobamos is es el Admin
					if (user.getAlias().equals("admin")) {
						admin = true;
					}
				}
				session.setAttribute("admin", admin);
				session.setAttribute("usuario", usuario);
				session.setAttribute("sameSession", true);
			}
			else {
				boolean sameSession = isLogin.getSessionId().equals(
						session.getId());
				session.setAttribute("sameSession", sameSession);
			}
			response.sendRedirect("index.jsp");
		}

		if (request.getParameter("logOut") != null) {
			getServletContext().removeAttribute("isLogin");
			session.removeAttribute("admin");
			session.removeAttribute("usuario");
			session.removeAttribute("sameSession");
			response.sendRedirect("index.jsp");
		}

	}

}
