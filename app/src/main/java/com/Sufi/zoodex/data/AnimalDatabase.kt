package com.Sufi.zoodex.data

object AnimalDatabase {
    val allAnimals = listOf(
        // ELECTR Type - Electric Animals
        AnimalData(1, "VOLT HOUND", "ELECTR", "A tactical cybernetic canine covered in static fur discharging high green voltage.", 120, 14, 8, 18, "⚡🐕", "CANINE"),
        AnimalData(2, "EMERALD COBRA", "ELECTR", "A bio-engineered serpent charging green electrical voltage that releases lightning shocks.", 95, 13, 8, 15, "⚡🐍", "REPTILE"),
        AnimalData(3, "STRAY DOG", "ELECTR", "A loyal local canine equipped with static-charge bark sensors and neural link nodes.", 100, 12, 10, 13, "🐕", "CANINE"),
        AnimalData(4, "MARKHOR", "ELECTR", "The grand national mountain goat of Pakistan, charging with massive corkscrew electrified horns.", 140, 18, 14, 14, "🐐", "UNGULATE"),
        AnimalData(5, "GOLDEN EAGLE", "ELECTR", "A grand high-altitude raptor executing supersonic dives charged with high static voltage.", 115, 16, 9, 19, "⚡🦅", "RAPTOR"),
        
        // VOID Type - Void/Dark Animals
        AnimalData(6, "STORM EAGLE", "VOID", "A majestic and legendary predator of the high peaks, channeling gravity-defying void pressure.", 100, 12, 6, 22, "🌌🦅", "RAPTOR"),
        AnimalData(7, "VOID FLYER", "VOID", "A deep-space bat specimen that uses gravitational sonar frequencies to locate targets.", 100, 11, 7, 21, "🌌🦇", "CHIROPTERA"),
        AnimalData(8, "HOUSE SPARROW", "VOID", "A tiny, extremely common local bird capable of briefly phasing through solid structures.", 80, 8, 5, 22, "🕊️", "AVIAN"),
        AnimalData(9, "COMMON CROW", "VOID", "An exceptionally intelligent scavenger bird capable of deciphering security encryption codes.", 95, 10, 7, 18, "🐦", "AVIAN"),
        AnimalData(10, "SHELTER CORAL", "VOID", "An bio-luminescent aquatic structure feeding directly on background cosmic rays.", 150, 10, 18, 5, "🪸", "AQUATIC"),
        AnimalData(11, "SNOW LEOPARD", "VOID", "The elusive ghost of northern peaks, breathing freezing volcanic steam waves.", 130, 17, 12, 16, "❄️🐆", "FELINE"),
        
        // FIRE Type - Fire Animals
        AnimalData(12, "MAGMA GORILLA", "FIRE", "A colossal primordial volcanic ape composed of pure obsidian plates and superheated lava.", 160, 20, 15, 8, "🔥🦍", "PRIMATE"),
        AnimalData(13, "SOLAR HAWK", "FIRE", "A high-altitude thermal raptor whose wings burn with intense nuclear solar fusion.", 105, 15, 7, 20, "🔥🦅", "RAPTOR"),
        AnimalData(14, "DESERT SAND-CAT", "FIRE", "A small desert feline that glides invisibly across glowing volcanic sand dunes.", 95, 12, 9, 17, "🔥🐱", "FELINE"),
        AnimalData(15, "MONAL PHEASANT", "FIRE", "A radiant avian displaying blazing solar plumes, living in sub-alpine shrub forests.", 90, 10, 8, 16, "🔥🐦", "AVIAN"),
        AnimalData(16, "HIMALAYAN IBEX", "FIRE", "A magnificent mountain ibex that walks along sheer vertical cliffs using gravity-bending hooves.", 135, 15, 13, 14, "🔥🐐", "UNGULATE"),
        
        // CYBER Type - Cyber/Tech Animals
        AnimalData(17, "NEON TIGER", "CYBER", "A glowing digital apex feline stalking the mainframe and composed of raw high-speed data energy flows.", 110, 16, 10, 16, "💻🐯", "FELINE"),
        AnimalData(18, "GLITCH SPECTER", "CYBER", "A phasing network anomaly flickering in compiled compiler memory buffers.", 95, 12, 8, 14, "💻👻", "PHANTOM"),
        AnimalData(19, "STREET CAT", "CYBER", "A highly common and agile urban feline integrated with neural data tracking nodes.", 90, 11, 8, 15, "🐱", "FELINE"),
        AnimalData(20, "PARK SQUIRREL", "CYBER", "A speedy rodent that stores micro-fusion power cores instead of acorns inside urban parks.", 85, 9, 6, 20, "💻🐿️", "RODENT"),
        AnimalData(21, "PALLAS CAT", "CYBER", "An extremely fluffy high-altitude wild feline with high-resolution telephoto ocular lenses.", 110, 13, 12, 12, "💻🐱", "FELINE"),
        AnimalData(22, "INDUS DOLPHIN", "CYBER", "A rare freshwater swimmer using high-precision spatial echolocation systems to map coordinates.", 115, 13, 11, 16, "💻🐬", "AQUATIC"),
        
        // WATER Type - Water Animals
        AnimalData(23, "RIVER OTTER", "WATER", "A sleek aquatic mammal gliding through freshwater channels with perfect hydrodynamic grace.", 105, 11, 10, 17, "💧🦦", "AQUATIC"),
        AnimalData(24, "KINGFISHER", "WATER", "A vibrant diving bird plunging into crystal waters to capture prey with surgical precision.", 85, 12, 7, 19, "💧🐦", "AVIAN"),
        AnimalData(25, "GIANT CATFISH", "WATER", "A massive freshwater predator lurking in deep river channels searching for vibrations.", 135, 14, 13, 10, "💧🐟", "AQUATIC"),
        AnimalData(26, "FROG", "WATER", "A humble amphibian with powerful legs and sticky tongue, hiding in pond vegetation.", 80, 8, 7, 14, "💧🐸", "AMPHIBIAN"),
        AnimalData(27, "GHARIAL", "WATER", "A long-snouted crocodilian lurking in Indian rivers, masters of stealth hunting.", 125, 15, 14, 12, "💧🐊", "REPTILE"),
        
        // EARTH Type - Ground/Rock Animals
        AnimalData(28, "INDIAN RHINOCEROS", "EARTH", "A massive armored giant with thick skin and a powerful horn, charging through grasslands.", 150, 18, 16, 9, "🪨🦏", "UNGULATE"),
        AnimalData(29, "TORTOISE", "EARTH", "An ancient shelled reptile moving slowly but with unshakeable defense.", 140, 9, 19, 5, "🪨🐢", "REPTILE"),
        AnimalData(30, "WILD BOAR", "EARTH", "A sturdy tusked mammal roaming forests with aggressive charging tactics.", 120, 16, 12, 11, "🪨🐗", "UNGULATE"),
        AnimalData(31, "BADGER", "EARTH", "A tough underground dweller with powerful claws and surprising ferocity.", 100, 13, 14, 12, "🪨🦡", "MUSTELID"),
        AnimalData(32, "BURROWING OWL", "EARTH", "A small ground-nesting owl with keen night vision and agile movements.", 75, 9, 8, 16, "🪨🦉", "AVIAN"),
        
        // EXOTIC/RARE Animals
        AnimalData(33, "BENGAL TIGER", "FIRE", "India's most fearsome predator, striped and deadly, ruling the forest with grace.", 140, 19, 11, 17, "🔥🐯", "FELINE"),
        AnimalData(34, "ASIAN ELEPHANT", "EARTH", "A colossal intelligent mammal with tusks and trunk, revered in cultures worldwide.", 170, 17, 17, 11, "🪨🐘", "PROBOSCIDEAN"),
        AnimalData(35, "SLOTH BEAR", "FIRE", "An aggressive forest dweller with shaggy fur and surprising speed despite its lazy name.", 115, 15, 13, 10, "🔥🐻", "URSIDAE"),
        AnimalData(36, "CLOUDED LEOPARD", "VOID", "An elusive tree-dwelling predator with long fangs and incredible climbing abilities.", 105, 16, 9, 18, "🌌🐆", "FELINE"),
        AnimalData(37, "PANGOLIN", "EARTH", "A scaly insectivore with an impenetrable armor of overlapping plates.", 95, 10, 17, 9, "🪨🦑", "PHOLIDOTA"),
        AnimalData(38, "HORNBILL", "CYBER", "A large intelligent bird with a massive beak and distinctive calls.", 95, 11, 10, 15, "💻🐦", "AVIAN"),
        AnimalData(39, "KING COBRA", "VOID", "The world's longest venomous snake, majestic and deadly with a raised hood.", 110, 14, 10, 14, "🌌🐍", "REPTILE"),
        AnimalData(40, "PEACOCK", "CYBER", "A strikingly beautiful bird with an iridescent tail displaying intricate patterns.", 90, 10, 9, 14, "💻🦚", "AVIAN"),
        AnimalData(41, "SAMBAR DEER", "WATER", "A large dark deer with impressive antlers roaming forest reserves.", 110, 12, 11, 15, "💧🦌", "CERVID"),
        AnimalData(42, "NILGAI ANTELOPE", "EARTH", "A horse-like antelope with striking coloration and powerful build.", 125, 15, 13, 14, "🪨🦌", "BOVID"),
        AnimalData(43, "WILD WATER BUFFALO", "WATER", "A massive horned bovid commanding respect through sheer size and strength.", 145, 17, 15, 10, "💧🐃", "BOVID"),
        AnimalData(44, "ASIAN PALM CIVET", "CYBER", "A nocturnal tree-dweller known for producing expensive coffee through digestion.", 85, 10, 8, 16, "💻🐱", "VIVERRIDAE"),
        AnimalData(45, "PEAFOWL", "FIRE", "A large ground bird displaying incredible iridescent plumage during courtship.", 95, 11, 9, 15, "🔥🦚", "AVIAN"),
        AnimalData(46, "HYENA", "VOID", "A powerful predator with bone-crushing jaws and eerie vocalizations.", 115, 14, 12, 13, "🌌🐕", "CANINE"),
        AnimalData(47, "JACKAL", "ELECTR", "A smaller cunning canine adapting to various habitats with intelligence.", 95, 11, 9, 14, "⚡🐕", "CANINE"),
        AnimalData(48, "MONITOR LIZARD", "FIRE", "A large predatory lizard with sharp claws and powerful tail strikes.", 110, 14, 11, 12, "🔥🦎", "REPTILE"),
        AnimalData(49, "FLYING SQUIRREL", "CYBER", "A tree-dwelling rodent with membranous wings allowing gliding flight.", 80, 9, 7, 18, "💻🐿️", "RODENT"),
        AnimalData(50, "FISHING CAT", "WATER", "A semi-aquatic feline with partially webbed paws for hunting in wetlands.", 100, 12, 10, 15, "💧🐱", "FELINE"),
        AnimalData(51, "SARUS CRANE", "WATER", "A tall elegant wading bird with striking red facial markings.", 95, 10, 8, 17, "💧🦢", "AVIAN"),
        AnimalData(52, "SACRED MONKEY", "CYBER", "An intelligent primate revered in many cultures, agile and social.", 100, 12, 9, 16, "💻🐵", "PRIMATE"),
        AnimalData(53, "PYTHON", "VOID", "A large non-venomous constrictor with hypnotic patterns on its scales.", 115, 13, 12, 11, "🌌🐍", "REPTILE"),
        AnimalData(54, "WILD DOG PACK ALPHA", "FIRE", "The dominant leader of a wild dog pack, fiercely loyal yet ruthless.", 125, 16, 13, 15, "🔥🐕", "CANINE"),
        AnimalData(55, "GREAT HORNBILL", "CYBER", "A massive bird with a distinctive casque on its beak, living in old-growth forests.", 105, 12, 11, 14, "💻🐦", "AVIAN"),
        AnimalData(56, "CLOUDY VISION BAT", "VOID", "A nocturnal mammal with exceptional echolocation capabilities for navigation.", 90, 10, 8, 17, "🌌🦇", "CHIROPTERA"),
        AnimalData(57, "TARANTULA HAWK", "ELECTR", "A massive wasp with one of the most painful stings in the insect world.", 85, 15, 7, 16, "⚡🦅", "INSECTA"),
        AnimalData(58, "WILD BOAR TUSKER", "EARTH", "A massive old boar with formidable tusks, aggressive and territorial.", 140, 18, 14, 10, "🪨🐗", "UNGULATE"),
        AnimalData(59, "PORCUPINE", "EARTH", "A spiky rodent covered in sharp quills for protection from predators.", 105, 11, 15, 8, "🪨🦔", "RODENT"),
        AnimalData(60, "MONGOOSE", "CYBER", "A quick-moving mustelid famous for hunting venomous snakes fearlessly.", 95, 13, 9, 17, "💻🦡", "MUSTELID")
    )

    fun getAnimalById(id: Int): AnimalData? = allAnimals.find { it.id == id }
    
    fun getAnimalsByClass(className: String): List<AnimalData> = allAnimals.filter { 
        it.encyclopediaClass.equals(className, ignoreCase = true) 
    }
    
    fun getAnimalsByElement(element: String): List<AnimalData> = allAnimals.filter { 
        it.elementType.equals(element, ignoreCase = true) 
    }
    
    fun searchAnimals(query: String): List<AnimalData> = allAnimals.filter {
        it.name.contains(query, ignoreCase = true) || 
        it.description.contains(query, ignoreCase = true)
    }
}
