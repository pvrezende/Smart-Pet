package com.paulo.smartpet.controller;

import com.paulo.smartpet.dto.ApiPageResponse;
import com.paulo.smartpet.dto.ApiSuccessResponse;
import com.paulo.smartpet.dto.CreateUserRequest;
import com.paulo.smartpet.dto.UserResponse;
import com.paulo.smartpet.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.list();
    }

    @GetMapping("/page")
    public ApiPageResponse<UserResponse> listPaged(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir
    ) {
        return userService.listPaged(active, role, search, page, size, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiSuccessResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiSuccessResponse.of("Usuário criado com sucesso", userService.create(request));
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        userService.deactivate(id);
    }
}