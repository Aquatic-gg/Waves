package gg.aquatic.kmenu

interface PaginatedMenu {

    suspend fun handleNextPage()
    suspend fun handlePreviousPage()

}