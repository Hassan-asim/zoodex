package com.Sufi.zoodex.util

object IconUtils {
    fun getAnimalIcon(animalName: String): String {
        val name = animalName.uppercase()
        return when {
            name.contains("DOG") || name.contains("HOUND") || name.contains("JACKAL") || name.contains("HYENA") -> "🐕"
            name.contains("COBRA") || name.contains("SNAKE") || name.contains("PYTHON") -> "🐍"
            name.contains("MARKHOR") || name.contains("IBEX") || name.contains("GOAT") -> "🐐"
            name.contains("EAGLE") || name.contains("HAWK") || name.contains("FALCON") -> "🦅"
            name.contains("FLYER") || name.contains("BAT") -> "🦇"
            name.contains("SPARROW") || name.contains("CROW") || name.contains("BIRD") || name.contains("PHEASANT") || name.contains("KINGFISHER") || name.contains("HORNBILL") || name.contains("CRANE") -> "🐦"
            name.contains("CORAL") -> "🪸"
            name.contains("TIGER") || name.contains("LEOPARD") || name.contains("CAT") || name.contains("CIVET") || name.contains("FELINE") -> "🐈"
            name.contains("GORILLA") || name.contains("MONKEY") || name.contains("PRIMATE") || name.contains("APE") -> "🦍"
            name.contains("SQUIRREL") -> "🐿️"
            name.contains("DOLPHIN") -> "🐬"
            name.contains("OTTER") -> "🦦"
            name.contains("FISH") -> "🐟"
            name.contains("FROG") -> "🐸"
            name.contains("GHARIAL") || name.contains("CROCODILE") || name.contains("ALLIGATOR") -> "🐊"
            name.contains("RHINOCEROS") || name.contains("RHINO") -> "🦏"
            name.contains("TORTOISE") || name.contains("TURTLE") -> "🐢"
            name.contains("BOAR") || name.contains("PIG") || name.contains("TUSKER") -> "🐗"
            name.contains("BADGER") || name.contains("MONGOOSE") || name.contains("MUSTELID") -> "🦡"
            name.contains("OWL") -> "🦉"
            name.contains("ELEPHANT") -> "🐘"
            name.contains("BEAR") -> "🐻"
            name.contains("PANGOLIN") -> "🛡️"
            name.contains("PEACOCK") || name.contains("PEAFOWL") -> "🦚"
            name.contains("DEER") || name.contains("ANTELOPE") || name.contains("SAMBAR") || name.contains("NILGAI") -> "🦌"
            name.contains("BUFFALO") -> "🐃"
            name.contains("LIZARD") || name.contains("MONITOR") -> "🦎"
            name.contains("PORCUPINE") -> "🦔"
            name.contains("SPECTER") || name.contains("GHOST") -> "👻"
            else -> "🐾"
        }
    }
}
