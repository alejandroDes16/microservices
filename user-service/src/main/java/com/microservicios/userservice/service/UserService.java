package com.microservicios.userservice.service;

import com.microservicios.userservice.entity.Usuario;
import com.microservicios.userservice.feignclients.BikeFeignClient;
import com.microservicios.userservice.feignclients.CarFeignClient;
import com.microservicios.userservice.models.Bike;
import com.microservicios.userservice.models.Car;
import com.microservicios.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CarFeignClient carFeignClient;

    @Autowired
    BikeFeignClient bikeFeignClient;

    public List<Usuario> getAll(){
        return userRepository.findAll();
    }

    public Usuario getUserById(int id){
        return userRepository.findById(id).orElse(null);
    }

    public Usuario save(Usuario user){
        Usuario userNew = userRepository.save(user);
        return userNew;
    }

    public List getCars(int userId){
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer "+jwt.getTokenValue());
        ResponseEntity<List> cars = restTemplate.exchange("http://car-service/car/byuser/" + userId, HttpMethod.GET, new HttpEntity<>(httpHeaders), List.class);
        return cars.getBody();
    }

    public List getBikes(int userId){
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer "+jwt.getTokenValue());
        ResponseEntity<List> bikes = restTemplate.exchange("http://bike-service/bike/byuser/" + userId,HttpMethod.GET, new HttpEntity<>(httpHeaders), List.class);
        return bikes.getBody();
    }

    public Car saveCar(int userId, Car car){
        car.setUserId(userId);
        Car newCar = carFeignClient.save(car);
        return newCar;
    }

    public Bike saveBike(int userId, Bike bike){
        bike.setUserId(userId);
        Bike newBike = bikeFeignClient.save(bike);
        return newBike;
    }

    public Map<String, Object> getUserAndVehicles(int userId){
        Map<String, Object> result = new HashMap<>();
        Usuario user = userRepository.findById(userId).orElse(null);
        if (user == null){
            result.put("Mensaje", "no existe el usuario");
            return result;
        }
        result.put("User", user);
        List<Car> cars = carFeignClient.getCars(userId);
        if (cars.isEmpty()){
            result.put("Cars", "ese user no tiene coches");

        }else{
            result.put("Cars", cars);
        }
        List<Bike> bikes = bikeFeignClient.getBikes(userId);
        if (bikes.isEmpty()){
            result.put("Bikes", "ese user no tiene motos");

        }else{
            result.put("Bikes", bikes);
        }
        return result;
    }




}
