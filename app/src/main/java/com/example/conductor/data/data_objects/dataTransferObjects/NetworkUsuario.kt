package com.example.conductor.data.data_objects.dataTransferObjects
/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.squareup.moshi.JsonClass
import com.example.conductor.data.data_objects.domainObjects.Usuario

/**
 * DataTransferObjects go in this file. These are responsible for parsing responses from the server
 * or formatting objects to send to the server. You should convert these to domain objects before
 * using them.
 */

/**
 * VideoHolder holds a list of Videos.
 *
 * This is to parse first level of our network result which looks like
 *
 * {
 *   "videos": []
 * }
 */
@JsonClass(generateAdapter = true)
data class NetworkUsuariosContainer(var usuarios: ArrayList<NetworkUsuario>)

/**
 * Videos represent a devbyte that can be played.
 */

@JsonClass(generateAdapter = true)
data class NetworkUsuario(
    val id: String,
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val usuario: String,
    val password: String,
    val deshabilitada: Boolean,
    val rol: String,
    )

/**
 * Convert Network results to database objects
 */
fun NetworkUsuariosContainer.asDomainModel(): List<Usuario> {
    return usuarios.map {
        Usuario(
            id = it.id,
            nombre = it.nombre,
            apellidoPaterno = it.apellidoPaterno,
            apellidoMaterno = it.apellidoMaterno,
            usuario = it.usuario,
            password = it.password,
            deshabilitada = it.deshabilitada,
            rol = it.rol,
        )
    }
}

/*fun NetworkAsteroidsContainer.asDatabaseModel(): Array<DatabaseAsteroidEntity> {
    return asteroids.map {
        DatabaseAsteroidEntity(
            id = it.id,
            codename = it.codename,
            closeApproachDate = it.closeApproachDate,
            absoluteMagnitude = it.absoluteMagnitude,
            estimatedDiameter = it.estimatedDiameter,
            relativeVelocity = it.relativeVelocity,
            distanceFromEarth = it.distanceFromEarth,
            isPotentiallyHazardous = it.isPotentiallyHazardous)
    }.toTypedArray()
}*/

