package ru.kata.spring.boot_security.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.kata.spring.boot_security.demo.configs.MvcConfig;
import ru.kata.spring.boot_security.demo.configs.SuccessUserHandler;
import ru.kata.spring.boot_security.demo.configs.WebSecurityConfig;
import ru.kata.spring.boot_security.demo.controller.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({
        UserRestController.class,
        RoleRestController.class,
        ProfileRestController.class
})
@Import({
        WebSecurityConfig.class,
        SuccessUserHandler.class,
        MvcConfig.class
})
class RestApiTest {
    @Autowired MockMvc mvc;
    @MockBean UserService users;
    @MockBean RoleService roles;
    private User example;
    private static final String BODY = "{\"name\":\"Anna\",\"lastName\":\"Test\",\"age\":25,"
            + "\"email\":\"anna@example.com\",\"password\":\"secret\",\"roleIds\":[1]}";

    @BeforeEach void setUp() {
        example = new User("Anna", "anna@example.com");
        example.setId(1L);
        example.setPassword("hashed-secret");
        when(users.findById(1L)).thenReturn(example);
        when(users.findAll()).thenReturn(List.of(example));
    }

    @Test void anonymousGets401() throws Exception {
        mvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());
    }

    @Test void userCannotAccessAdminEndpoints() throws Exception {
        mvc.perform(get("/api/admin/users").with(user("reader").roles("USER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/roles").with(user("reader").roles("USER")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/users").with(user("reader").roles("USER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/users/1").with(user("reader").roles("USER")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/admin/users/1").with(user("reader").roles("USER")).with(csrf()))
                .andExpect(status().isForbidden());
        verify(users, never()).deleteById(anyLong());
    }

    @Test void listDoesNotExposePassword() throws Exception {
        mvc.perform(get("/api/admin/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].email").value("anna@example.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test void csrfIsRequired() throws Exception {
        mvc.perform(post("/api/admin/users").with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden());
    }

    @Test void createUpdateDelete() throws Exception {
        mvc.perform(post("/api/admin/users").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isCreated());
        verify(users).createUser("Anna", "Test", 25, "anna@example.com", "secret", List.of(1L));
        mvc.perform(put("/api/admin/users/1").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(BODY.replace("secret", "")))
                .andExpect(status().isNoContent());
        verify(users).updateUser(1L, "Anna", "Test", 25, "anna@example.com", "", List.of(1L));
        mvc.perform(delete("/api/admin/users/1").with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isNoContent());
        verify(users).deleteById(1L);
    }

    @Test void missingUserGets404() throws Exception {
        mvc.perform(get("/api/admin/users/99").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").exists());
        mvc.perform(delete("/api/admin/users/99").with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test void validationAndDuplicateErrorsAreJson() throws Exception {
        mvc.perform(post("/api/admin/users").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(BODY.replace("25", "-1")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        doThrow(new IllegalArgumentException("Email уже занят")).when(users)
                .createUser(anyString(), anyString(), anyInt(), anyString(), anyString(), anyList());
        mvc.perform(post("/api/admin/users").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Email уже занят"));
    }

    @Test void profileReadsFreshDataByPrincipalId() throws Exception {
        mvc.perform(get("/api/user").with(user(example)))
                .andExpect(status().isForbidden());
        ru.kata.spring.boot_security.demo.model.Role role = new ru.kata.spring.boot_security.demo.model.Role("ROLE_USER");
        role.setId(1L);
        example.setRoles(java.util.Set.of(role));
        mvc.perform(get("/api/user").with(user(example)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test void pagesRenderWithoutUserModelAttributes() throws Exception {
        mvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(view().name("users"));
        mvc.perform(get("/user").with(user("reader").roles("USER")))
                .andExpect(status().isOk()).andExpect(view().name("user"));
    }
}
