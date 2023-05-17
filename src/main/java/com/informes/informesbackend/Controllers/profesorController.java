package com.informes.informesbackend.Controllers;

import com.informes.informesbackend.Models.Entities.Alumno;
import com.informes.informesbackend.Models.Entities.Profesor;

import com.informes.informesbackend.Services.ProfesorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.*;

@RestController()
@RequestMapping("/profesor")
@CrossOrigin("*")
public class profesorController {

    @Autowired
    private ProfesorService service;




    @GetMapping("/list")
    public ResponseEntity<List<Profesor>> listar(){
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/list/{id}")
    public ResponseEntity<?> detalle(@PathVariable Long id){
        Optional<Profesor> usuarioOptional= service.listarporId(id);
        if (usuarioOptional.isPresent()){
            return ResponseEntity.ok(usuarioOptional.get());
        }
        return ResponseEntity.notFound().build();
    }


    @PostMapping("/save")
    public ResponseEntity<?> crear( @RequestBody Profesor profesor, BindingResult result) {
        if (!profesor.getEmail().isEmpty() && service.porEmail(profesor.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("Mensaje", "El email ingresado ya existe"));
        }
        if (!profesor.getDni().isEmpty() && service.listarporDni(profesor.getDni()).isPresent()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("Mensaje", "El profesor ingresado ya existe"));
        }
        if (result.hasErrors()) {
            return validar(result);
        }

        Set<String> roles= new HashSet<>();
        roles.add("profesor");

        Profesor ProfesorGuardado = service.guardar(profesor);

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(ProfesorGuardado.getId()).toUri();

        // crear el usuario para el alumno

      //  NuevoUsuario nuevoUsuario= new NuevoUsuario();
       // nuevoUsuario.setNombreUsuario(profesor.getDni());
       // nuevoUsuario.setPassword(profesor.getDni());
       // nuevoUsuario.setRoles(roles);

       // guardarUsuarioService.crearUsuario(nuevoUsuario);


        return ResponseEntity.created(ubicacion).body(ProfesorGuardado);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> editar( @RequestBody Profesor profesor, @PathVariable Long id) {



        Optional<Profesor> profesorOptional= service.listarporId(id);
        if (!profesorOptional.isPresent()) {
            return ResponseEntity.unprocessableEntity().build();
        }


        profesor.setId(profesorOptional.get().getId());
        service.guardar(profesor);

        return ResponseEntity.noContent().build();


    }


    @DeleteMapping("/delete/{id}")

    public ResponseEntity<Alumno> eliminar(@PathVariable Long id){
        Optional<Profesor> profesorOptional = service.listarporId(id);

        if(!profesorOptional.isPresent()){
            return ResponseEntity.unprocessableEntity().build();
        }

       // Optional<Usuario> usuario= usuarioService.getByNombreUsuario( profesorOptional.get().getDni());

       // usuarioService.delete(usuario.get().getId());

        service.eliminar(profesorOptional.get().getId());
        return ResponseEntity.ok().build();
    }

    private static ResponseEntity<Map<String, String>> validar(BindingResult result) {
        Map<String,String> errores= new HashMap<>();
        result.getFieldErrors().forEach(err -> {
            errores.put(err.getField(), "el campo "+err.getField()+" "+err.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errores);}
}
