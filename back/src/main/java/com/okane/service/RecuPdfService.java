package com.okane.service;

import com.okane.entity.Transfert;
import com.okane.util.PdfGenerator;

import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

public interface RecuPdfService {
    byte[] genererRecu(Transfert transfert) throws Exception;
    byte[] genererRecuParId(Long transfertId) throws Exception;
}
