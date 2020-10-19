package com.arny.flightlogbook.customfields.domain

import com.arny.flightlogbook.customfields.models.CustomFieldValue
import org.junit.Test


class CustomFieldInteractorTest {


    @Test
    fun `remove only onther ids`() {
        val idsToRemove = getIdsToRemove(
                listOf(CustomFieldValue(1), CustomFieldValue(2), CustomFieldValue(3)),
                listOf(CustomFieldValue(1))
        ).map { it.id }

        assert(idsToRemove == listOf<Long>(2, 3))
    }

    @Test
    fun `try to remove EXPECT empty`() {
        val idsToRemove = getIdsToRemove(
                listOf(CustomFieldValue(1)),
                listOf(CustomFieldValue(1))
        ).map { it.id }

        assert(idsToRemove.isEmpty())
    }


    @Test
    fun `remove ids all`() {
        val idsToRemove = getIdsToRemove(
                listOf(CustomFieldValue(1)),
                listOf()
        ).map { it.id }

        assert(idsToRemove == listOf<Long>(1))
    }


    fun getIdsToRemove(
            origin: List<CustomFieldValue>,
            newList: List<CustomFieldValue>
    ): List<CustomFieldValue> = origin.filter { listValue ->
        newList.find { it.id == listValue.id } == null
    }
}