package gg.aquatic.kregistry

interface RegistryHolder {

    fun registryBootstrap(builder: ContributionBuilder.() -> Unit) {
        val contrBuilder = ContributionBuilder()
        contrBuilder.builder()

        val contribution = contrBuilder.build(this)
        RegistryBootstrap.injectContribution(contribution)
    }
}