package com.example.conductor

import com.example.conductor.data.AppDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class AuthenticationActivityTest{

    /*@get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var firebaseAuth: FirebaseAuth

    @Mock
    private lateinit var cloudDB: FirebaseFirestore

    @Mock
    private lateinit var appDataSource: AppDataSource

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }*/

    @Test
    fun chequearLogin(){
     /*   val activity = AuthenticationActivity().apply {
            firebaseAuth = this@AuthenticationActivityTest.firebaseAuth
            cloudDB = this@AuthenticationActivityTest.cloudDB
            dataSource = this@AuthenticationActivityTest.appDataSource
        }*/

        val resultado = AuthenticationActivity().hayUsuarioLogeado(null)
        assertEquals(false, resultado)
    }
}