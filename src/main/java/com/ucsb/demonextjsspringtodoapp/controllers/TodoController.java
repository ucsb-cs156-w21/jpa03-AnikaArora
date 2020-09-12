package com.ucsb.demonextjsspringtodoapp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucsb.demonextjsspringtodoapp.models.Todo;
import com.ucsb.demonextjsspringtodoapp.repositories.TodoRepository;
import com.ucsb.demonextjsspringtodoapp.services.Auth0Service;

@RestController
public class TodoController {
  private final Logger logger = LoggerFactory.getLogger(TodoController.class);

  @Autowired
  private TodoRepository todoRepository;

  @Autowired
  private Auth0Service auth0Service;

  @PostMapping(value = "/api/todos", produces = "application/json")
  public ResponseEntity<String> createTodo(@RequestHeader("Authorization") String authorization,
      @RequestBody Todo todo) throws JsonProcessingException {
    DecodedJWT jwt = JWT.decode(authorization.substring(7));
    todo.setUserId(jwt.getSubject());
    Todo savedTodo = todoRepository.save(todo);
    ObjectMapper mapper = new ObjectMapper();
    String body = mapper.writeValueAsString(savedTodo);
    return ResponseEntity.ok().body(body);
  }

  @PostMapping(value = "/api/todos/{id}", produces = "application/json")
  public ResponseEntity<String> updateTodo(@RequestHeader("Authorization") String authorization,
      @PathVariable("id") Long id) {
    DecodedJWT jwt = JWT.decode(authorization.substring(7));
    Optional<Todo> todo = todoRepository.findById(id);
    if (!todo.isPresent() || !todo.get().getUserId().equals(jwt.getSubject())) {
      return ResponseEntity.notFound().build();
    }
    todo.get().setDone(!todo.get().getDone());
    todoRepository.save(todo.get());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping(value = "/api/todos/{id}", produces = "application/json")
  public ResponseEntity<String> deleteTodo(@RequestHeader("Authorization") String authorization,
      @PathVariable("id") Long id) {
    DecodedJWT jwt = JWT.decode(authorization.substring(7));
    Optional<Todo> todo = todoRepository.findById(id);
    if (!todo.isPresent() || !todo.get().getUserId().equals(jwt.getSubject())) {
      return ResponseEntity.notFound().build();
    }
    todoRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping(value = "/api/todos", produces = "application/json")
  public ResponseEntity<String> getUserTodos(@RequestHeader("Authorization") String authorization)
      throws JsonProcessingException {
    DecodedJWT jwt = JWT.decode(authorization.substring(7));
    List<Todo> todoList = todoRepository.findByUserId(jwt.getSubject());
    ObjectMapper mapper = new ObjectMapper();

    String body = mapper.writeValueAsString(todoList);
    return ResponseEntity.ok().body(body);
  }
}
