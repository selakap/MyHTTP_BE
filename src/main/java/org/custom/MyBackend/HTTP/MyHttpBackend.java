package org.custom.MyBackend.HTTP;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class MyHttpBackend {
    static String badRequest = "400 Bad Request";
    static String notFound = "404 Not Found";
    static String ok = "200 OK";
    static String responseMessage = null;
    static String responseCode = null;
    public static void main(String[] args) {
        int port = 8000; // the port to listen on
        //curl -X GET http://localhost:8000/hardware/deviceUnit?device_type=macOs
        //curl -X GET http://localhost:8000/hardware/allDevices



        try {
            //Open a socket
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("HTTP backend listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // read the request
                StringBuilder requestBuilder = new StringBuilder();
                byte[] buffer = new byte[10];
                int read;

                do {
                    read = clientSocket.getInputStream().read(buffer);
                    if (read > 0) {
                        requestBuilder.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
                    }
                    //considering only header. when header segment end this will break the request read
                    if (requestBuilder.toString().contains("\r\n\r\n")){
                        break;
                    }
                } while (read == buffer.length);

                if (requestBuilder.toString().contains("\r\n\r\n")){
                    String headers = requestBuilder.toString().split("\r\n\r\n")[0];
                    System.out.println("headers: "+headers);

                    List<String> headerList = List.of(headers.split("\r\n"));

                    if (!headerList.get(0).isEmpty()){
                        if (headerList.get(0).contains("GET")){
                            String restUrlPostfix = headerList.get(0).split(" ")[1];
                            if (restUrlPostfix.split("\\?")[0].equals("/hardware/deviceUnit")) {
                                deviceUnit(restUrlPostfix);
                            } else if (restUrlPostfix.split("\\?")[0].equals("/hardware/allDevices")){
                                allDevices();
                            } else {
                                // send the 404 response
                                responseCode = notFound;
                                responseMessage = "{\"404\":\"requested resource in not found\"}";
                            }
                        } else{
                            // send the 404 response
                            responseCode = notFound;
                            responseMessage = "{\"404\":\"Please use GET method only\"}";
                        }
                    } else {
                        // send the 400 response
                        responseCode = badRequest;
                        responseMessage = "{\"400\":\"Please check the request and correct it according to the HTTP specs\"}";
                    }
                } else {
                    responseCode = badRequest;
                    responseMessage = "{\"400\":\"malformed request\"}";
                }

                // response
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                sendResponse(out);
                clientSocket.getInputStream().close();
                clientSocket.close();


            }


        } catch (SocketTimeoutException e) {
            System.out.println("Timed out waiting for client connection");
        } catch (IOException e) {
            System.out.println("Error while accepting client connection: " + e.getMessage());
        }
    }

    public static void allDevices() {
        responseCode = ok;
        responseMessage = "{\n" +
                "  \"entityTypeList\": [\n" +
                "    {\n" +
                "      \"deviceType\": \"ANDROID\",\n" +
                "      \"deviceTypeDescription\": \"ANDROID\",\n" +
                "      \"creationDate\": \"2018-09-04T13:38:49.000Z\",\n" +
                "      \"updateDate\": \"2018-09-04T13:38:49.000Z\",\n" +
                "      \"attributes\": [\n" +
                "        \n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"deviceType\": \"Childraspberry3735846_063\",\n" +
                "      \"deviceTypeDescription\": \"device_type_fot_sm\",\n" +
                "      \"creationDate\": \"2018-10-01T05:35:03.000Z\",\n" +
                "      \"updateDate\": \"2018-10-01T05:35:03.000Z\",\n" +
                "      \"attributes\": [\n" +
                "        \n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"deviceType\": \"Childraspberry457091_060\",\n" +
                "      \"deviceTypeDescription\": \"device_type_fot_sm\",\n" +
                "      \"creationDate\": \"2018-10-01T05:52:51.000Z\",\n" +
                "      \"updateDate\": \"2018-10-01T05:52:51.000Z\",\n" +
                "      \"attributes\": [\n" +
                "        \n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    public static void deviceUnit(String restUrlPostfix) {


        if (restUrlPostfix.split("\\?").length==2){
            String queryParam = restUrlPostfix.split("\\?")[1];
            if (!queryParam.contains("&")){
                if (queryParam.contains("=")){
                    if (queryParam.split("=")[0].equals("device_type")){
                        if (queryParam.split("=")[1].equalsIgnoreCase("ANDROID")){
                            responseCode = ok;
                            responseMessage = "{\"GOOGLE\":\"america\"}";
                        } else if (queryParam.split("=")[1].equalsIgnoreCase("macOS")){
                            responseCode = ok;
                            responseMessage = "{\"APPLE\":\"america\"}";
                        } else {
                            responseCode = ok;
                            responseMessage = "{\"unknown\":\"unknown\"}";
                        }
                    } else {
                        // send the 400 response
                        responseCode = badRequest;
                        responseMessage = "{\"400\":\"expect device_type as the query param\"}";
                    }
                } else {
                    // send the 400 response
                    responseCode = badRequest;
                    responseMessage = "{\"400\":\"malformed query param\"}";
                }

            } else {
                // send the 400 response
                responseCode = badRequest;
                responseMessage = "{\"400\":\"expect only one query param \"}";
            }
        } else {
            // send the 400 response
            responseCode = badRequest;
            responseMessage = "{\"400\":\"expect atleast and only one query param\"}";
        }
    }



    public static void sendResponse(PrintWriter out) {
        out.print("HTTP/1.1 "+responseCode+"\r\n");
        out.print("Content-Length: "+responseMessage.length()+"\r\n");
        out.print("Content-Type: application/json; charset=UTF-8\r\n");
        out.print("\r\n");
        out.print(responseMessage);

        out.flush();
        out.close();
        responseCode = null;
        responseMessage = null;
    }

}
