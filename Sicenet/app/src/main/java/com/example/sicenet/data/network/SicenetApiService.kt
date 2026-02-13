package com.example.sicenet.data.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface SicenetApiService {

    @Headers(
        "Host: sicenet.surguanajuato.tecnm.mx",
        "Content-Type: text/xml; charset=\"UTF-8\"",
        "SOAPAction: \"http://tempuri.org/accesoLogin\"",
        "Cookie: .ASPXANONYMOUS=MaWJCZ-X2gEkAAAAODU2ZjkyM2EtNWE3ZC00NTdlLWFhYTAtYjk5ZTE5MDlkODIzeI1pCwvskL6aqtre4eT8Atfq2Po1;"
    )
    @POST("ws/wsalumnos.asmx")
    suspend fun acceso(@Body soap: RequestBody): Response<ResponseBody>

    @Headers("Content-Type: text/xml; charset=utf-8")
    @POST("ws/wsalumnos.asmx")
    suspend fun getProfile(
        @Header("SOAPAction") soapAction: String,
        @Header("Cookie") cookie: String?,
        @Body soap: RequestBody
    ): Response<ResponseBody>

    @Headers("Content-Type: text/xml; charset=utf-8")
    @POST("ws/wsalumnos.asmx")
    suspend fun getCargaAcademica(
        @Header("SOAPAction") soapAction: String = "\"http://tempuri.org/getCargaAcademicaByAlumno\"",
        @Header("Cookie") cookie: String?,
        @Body soap: RequestBody
    ): Response<ResponseBody>

    @Headers("Content-Type: text/xml; charset=utf-8")
    @POST("ws/wsalumnos.asmx")
    suspend fun getKardex(
        @Header("SOAPAction") soapAction: String = "\"http://tempuri.org/getAllKardexConPromedioByAlumno\"",
        @Header("Cookie") cookie: String?,
        @Body soap: RequestBody
    ): Response<ResponseBody>

    @Headers("Content-Type: text/xml; charset=utf-8")
    @POST("ws/wsalumnos.asmx")
    suspend fun getCalifUnidades(
        @Header("SOAPAction") soapAction: String = "\"http://tempuri.org/getCalifUnidadesByAlumno\"",
        @Header("Cookie") cookie: String?,
        @Body soap: RequestBody
    ): Response<ResponseBody>

    @Headers("Content-Type: text/xml; charset=utf-8")
    @POST("ws/wsalumnos.asmx")
    suspend fun getCalifFinal(
        @Header("SOAPAction") soapAction: String = "\"http://tempuri.org/getAllCalifFinalByAlumnos\"",
        @Header("Cookie") cookie: String?,
        @Body soap: RequestBody
    ): Response<ResponseBody>

    companion object {
        private const val BASE_URL = "https://sicenet.surguanajuato.tecnm.mx/"

        fun create(): SicenetApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(SicenetApiService::class.java)
        }
    }
}
