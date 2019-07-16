package com.arny.domain.models

data class Migrations(var id: Long = 0) {
    var filename: String = ""
    var applytime: String = ""
    override fun toString(): String {
        return "Migrations(id=$id, filename='$filename', applytime='$applytime')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Migrations

        if (id != other.id) return false
        if (filename != other.filename) return false
        if (applytime != other.applytime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + applytime.hashCode()
        return result
    }
}
