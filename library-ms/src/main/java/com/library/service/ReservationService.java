package com.library.service;

import com.library.dto.request.ReservationRequest;
import com.library.model.Reservation;

import java.util.List;

public interface ReservationService {

    Reservation placeReservation(ReservationRequest request);

    Reservation cancelReservation(String reservationId, String patronId);

    List<Reservation> getReservationsByPatron(String patronId);

    List<Reservation> getPendingReservationsByIsbn(String isbn);
}
