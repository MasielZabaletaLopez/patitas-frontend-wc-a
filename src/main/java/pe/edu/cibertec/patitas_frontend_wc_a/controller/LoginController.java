package pe.edu.cibertec.patitas_frontend_wc_a.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.patitas_frontend_wc_a.client.AutenticacionClient;
import pe.edu.cibertec.patitas_frontend_wc_a.dto.LoginRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc_a.dto.LoginResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc_a.viewmodel.LoginModel;
import reactor.core.publisher.Mono;


@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    WebClient webClientAutenticacion;

    @Autowired
    AutenticacionClient autenticacionClient;

    @GetMapping("/inicio")
    public String inicio(Model model) {
        LoginModel loginModel = new LoginModel("00", "", "");
        model.addAttribute("loginModel", loginModel);
        return "inicio";
    }

    @PostMapping("/autenticar")
    public String autenticar(@RequestParam("tipoDocumento") String tipoDocumento,
                             @RequestParam("numeroDocumento") String numeroDocumento,
                             @RequestParam("password") String password,
                             Model model) {

        System.out.println("Consuming with RestTemplate!!!");

        // Validar campos de entrada
        if (tipoDocumento == null || tipoDocumento.trim().length() == 0 ||
                numeroDocumento == null || numeroDocumento.trim().length() == 0 ||
                password == null || password.trim().length() == 0) {

            LoginModel loginModel = new LoginModel("01", "Error: Debe completar correctamente sus credenciales", "");
            model.addAttribute("loginModel", loginModel);
            return "inicio";

        }

        try {

            // invocar servicio de autenticación
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(tipoDocumento, numeroDocumento, password);
            Mono<LoginResponseDTO> monoLoginResponseDTO = webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class);

            // recuperar resultado modo bloqueante (Sincrónico)
            LoginResponseDTO loginResponseDTO = monoLoginResponseDTO.block();

            if (loginResponseDTO.codigo().equals("00")){

                LoginModel loginModel = new LoginModel("00", "", loginResponseDTO.nombreUsuario());
                model.addAttribute("loginModel", loginModel);
                return "principal";

            } else {

                LoginModel loginModel = new LoginModel("02", "Error: Autenticación fallida", "");
                model.addAttribute("loginModel", loginModel);
                return "inicio";

            }

        } catch(Exception e) {

            LoginModel loginModel = new LoginModel("99", "Error: Ocurrió un problema en la autenticación", "");
            model.addAttribute("loginModel", loginModel);
            System.out.println(e.getMessage());
            return "inicio";

        }

    }

    @PostMapping("/autenticar-feign")
    public String autenticarFeign(@RequestParam("tipoDocumento") String tipoDocumento,
                                  @RequestParam("numeroDocumento") String numeroDocumento,
                                  @RequestParam("password") String password,
                                  Model model) {

        System.out.println("Consuming with Feign Client!!!");

        // Validar campos de entrada
        if (tipoDocumento == null || tipoDocumento.trim().length() == 0 ||
                numeroDocumento == null || numeroDocumento.trim().length() == 0 ||
                password == null || password.trim().length() == 0) {

            LoginModel loginModel = new LoginModel("01", "Error: Debe completar correctamente sus credenciales", "");
            model.addAttribute("loginModel", loginModel);
            return "inicio";

        }

        try {

            // preparar request
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(tipoDocumento, numeroDocumento, password);

            // consumir servicio con Feign Client
            ResponseEntity<LoginResponseDTO> responseEntity = autenticacionClient.login(loginRequestDTO);

            // validar respuesta del servicio
            if (responseEntity.getStatusCode().is2xxSuccessful()) {

                // recuperar response
                LoginResponseDTO loginResponseDTO = responseEntity.getBody();

                if (loginResponseDTO.codigo().equals("00")){

                    LoginModel loginModel = new LoginModel("00", "", loginResponseDTO.nombreUsuario());
                    model.addAttribute("loginModel", loginModel);
                    return "principal";

                } else {

                    LoginModel loginModel = new LoginModel("02", "Error: Autenticación fallida", "");
                    model.addAttribute("loginModel", loginModel);
                    return "inicio";

                }

            } else {

                LoginModel loginModel = new LoginModel("99", "Error: Ocurrió un problema http", "");
                model.addAttribute("loginModel", loginModel);
                return "inicio";

            }

        } catch(Exception e) {

            LoginModel loginModel = new LoginModel("99", "Error: Ocurrió un problema en la autenticación", "");
            model.addAttribute("loginModel", loginModel);
            System.out.println(e.getMessage());
            return "inicio";

        }

    }

}
