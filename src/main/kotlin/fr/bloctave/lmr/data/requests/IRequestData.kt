package fr.bloctave.lmr.data.requests

interface IRequestData {



    fun getById(id: Int): Request?

    fun getAll(): Set<Request>
}