/*
 * The MIT License
 *
 * Copyright 2025 karla.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package br.com.motogestor.util;

import java.security.MessageDigest;

public class CriptografiaUtil {
    
    
    public static String criptografar(String senhaOriginal) {
        
        try {
            
            //Algoritmo de Hash (SHA-256)
            MessageDigest algoritimo = MessageDigest.getInstance("SHA-256");
            
            // Transforma a senha em bytes usando o padrão UTF-8 (para aceitar acentos, etc)
            byte[] messageDigest = algoritimo.digest(senhaOriginal.getBytes("UTF-8"));
            
            // Convertendo os bytes para Hexadecimal (texto)
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : messageDigest) {
                
                //Essa linha converte o byte em representação Hexadecimal
                hexString.append(String.format("%02X", 0xFF & b));
            }
            
        System.out.println("Senha Original: " + senhaOriginal);
        System.out.println("Hash Gerado: " + hexString.toString());
        
            //Retorna a senha "embaralhada"
            return hexString.toString();
            
        } catch (Exception e) {
            
            throw new RuntimeException("Erro ao criptografar senha", e);
        }
    }
}


