package fr.bloctave.lmr.data.requests

class ClientRequest : IRequestData {

    private val requests = mutableSetOf<Request>()

    override fun getById(id: Int): Request? = requests.find { it.id == id }

    override fun getAll(): Set<Request> = requests.toSet()

    fun add(request: Request): Int {
        println("YAHOUUUU")
        requests.add(request)
        return request.id
    }

    fun delete(requestId: Int): Boolean {
        return requests.removeIf { it.id == requestId }
    }

}