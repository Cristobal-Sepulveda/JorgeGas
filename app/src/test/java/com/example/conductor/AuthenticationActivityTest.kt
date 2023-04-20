package com.example.conductor

import androidx.test.core.app.ActivityScenario
import com.example.conductor.data.AppDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/*
@RunWith(AndroidJUnit4::class)
class AuthenticationActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var firebaseAuth: FirebaseAuth

    @Mock
    lateinit var cloudDB: FirebaseFirestore

    @Mock
    lateinit var appDataSource: AppDataSource

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun chequearLogin() {
        ActivityScenario.launch(AuthenticationActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.firebaseAuth = firebaseAuth
                activity.cloudDB = cloudDB
                activity.dataSource = appDataSource

                val resultado = activity.hayUsuarioLogeado(null)
                assertEquals(false, resultado)
            }
        }
    }
}*/
