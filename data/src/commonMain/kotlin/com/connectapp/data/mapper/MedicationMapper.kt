package com.connectapp.data.mapper

import com.connectapp.data.database.entity.MedicationEntity
import com.connectapp.data.model.MedicationDto
import com.connectapp.domain.model.Medication

fun Medication.toDto(): MedicationDto = MedicationDto(
    id = this.id,
    name = this.name,
    dosesPerDay = this.dosesPerDay,
    quantity = this.quantity,
    quantityUnit = this.quantityUnit,
    durationDays = this.durationDays,
    startDate = this.startDate,
    endDate = this.endDate,
    reminderMinutes = this.reminderMinutes,
    startTimeHour = this.startTimeHour,
    startTimeMinute = this.startTimeMinute,
    codUser = this.codUser
)

fun Medication.toEntity(): MedicationEntity = MedicationEntity(
    id = this.id,
    name = this.name,
    codUser = this.codUser,
    dosesPerDay = this.dosesPerDay,
    quantity = this.quantity,
    quantityUnit = this.quantityUnit,
    durationDays = this.durationDays,
    startDate = this.startDate,
    endDate = this.endDate,
    reminderMinutes = this.reminderMinutes,
    startTimeHour = this.startTimeHour,
    startTimeMinute = this.startTimeMinute
)

fun MedicationEntity.toDomain(): Medication = Medication(
    id = this.id,
    name = this.name,
    codUser = this.codUser,
    dosesPerDay = this.dosesPerDay,
    quantity = this.quantity,
    quantityUnit = this.quantityUnit,
    durationDays = this.durationDays,
    startDate = this.startDate,
    endDate = this.endDate,
    reminderMinutes = this.reminderMinutes,
    startTimeHour = this.startTimeHour,
    startTimeMinute = this.startTimeMinute
)

fun MedicationDto.toDomain(id: Long = 0): Medication = Medication(
    id = if (this.id != 0L) this.id else id,
    name = this.name,
    codUser = this.codUser,
    dosesPerDay = this.dosesPerDay,
    quantity = this.quantity,
    quantityUnit = this.quantityUnit,
    durationDays = this.durationDays,
    startDate = this.startDate,
    endDate = this.endDate,
    reminderMinutes = this.reminderMinutes,
    startTimeHour = this.startTimeHour,
    startTimeMinute = this.startTimeMinute
)
