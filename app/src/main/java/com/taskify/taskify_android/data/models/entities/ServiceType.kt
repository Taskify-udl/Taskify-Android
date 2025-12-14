package com.taskify.taskify_android.data.models.entities

// Enum representing different categories of services
enum class ServiceType {
    ABOGACIA,
    ACTIVIDADES_AL_AIRE_LIBRE,
    AGENCIA_DE_VIAJES,
    ALBANILERIA,
    ALOJAMIENTO_Y_ALQUILER_VACACIONAL,
    ALQUILER_DE_VEHICULOS,
    ANIMACION_2D_3D,
    ARQUITECTURA,
    ARTESANIA,
    ASESORIA_FISCAL_Y_CONTABLE,
    ASISTENCIA_A_DOMICILIO,
    ASISTENCIA_VIRTUAL,
    AUTOMATIZACION_E_IA,
    CARPINTERIA,
    CATERING,
    CERRAJERIA,
    CIBERSEGURIDAD,
    CLASES_DE_ARTE_Y_DIBUJO,
    CLASES_DE_MUSICA,
    CLASES_PARTICULARES,
    CLIMATIZACION,
    COCINA_A_DOMICILIO,
    COMMUNITY_MANAGER,
    CONSULTORIA_EMPRESARIAL,
    CONSULTORIA_IT,
    CUIDADO_DE_MASCOTAS,
    CUIDADO_DE_MAYORES,
    CURSOS_TECNICOS,
    DECORACION,
    DECORACION_DE_INTERIORES,
    DEPORTES_ACUATICOS,
    DESARROLLO_MOVIL,
    DESARROLLO_WEB,
    DISENO_GRAFICO,
    E_COMMERCE,
    EDICION_DE_IMAGENES,
    EDICION_DE_VIDEO,
    ELECTRICIDAD,
    ENTRENADOR_PERSONAL,
    ESTETICA_Y_MAQUILLAJE,
    FERRETERIA,
    FISIOTERAPIA,
    FLORISTERIA,
    FONTANERIA,
    FORMACION_ONLINE,
    FOTOGRAFIA,
    GESTION_DE_PROYECTOS,
    GRUA_Y_ASISTENCIA,
    GUIA_TURISTICO,
    IDIOMAS,
    IMPRESORAS_Y_PERIFERICOS,
    INGENIERIA,
    INSTALACION_DE_REDES,
    JARDINERIA,
    JOYERIA,
    LIBRERIA,
    LIMPIEZA,
    LIMPIEZA_INDUSTRIAL,
    MANTENIMIENTO_GENERAL,
    MARKETING_DIGITAL,
    MASAJES,
    MENSAJERIA,
    MENTORING,
    MODA_Y_ROPA,
    MUDANZAS,
    NINERAS,
    NUTRICION,
    ORGANIZACION_DE_EVENTOS,
    PASTELERIA_PERSONALIZADA,
    PELUQUERIA,
    PINTURA,
    PSICOLOGIA,
    RECURSOS_HUMANOS,
    REDACCION_Y_COPYWRITING,
    REFORMAS_DEL_HOGAR,
    REPARACION_DE_ELECTRODOMESTICOS,
    REPARACION_DE_MOVILES,
    REPARACION_DE_ORDENADORES,
    SEGURIDAD_Y_CCTV,
    SEO_SEM,
    SERVICIOS_CLOUD,
    SOPORTE_TECNICO,
    TAXI_VTC,
    TIENDA_MINORISTA,
    TRADUCCION,
    TRANSPORTE_DE_MERCANCIAS,
    TRANSPORTE_EN_BICI_MOTO,
    WEDDING_PLANNER,
    OTHER
}

/**
 * Mapa de totes les categories de serveis disponibles.
 * Clau: ID seqüencial (Int), Valor: Nom de la categoria (String).
 */
val SERVICE_TYPES = mapOf(
    39 to "Abogacía",
    74 to "Actividades al aire libre",
    73 to "Agencia de viajes",
    6 to "Albañilería",
    75 to "Alojamiento y alquiler vacacional",
    62 to "Alquiler de vehículos",
    19 to "Animación 2D/3D",
    44 to "Arquitectura",
    79 to "Artesanía",
    40 to "Asesoría fiscal y contable",
    30 to "Asistencia a domicilio",
    27 to "Asistencia virtual",
    88 to "Automatización e IA",
    5 to "Carpintería",
    66 to "Catering",
    10 to "Cerrajería",
    86 to "Ciberseguridad",
    52 to "Clases de arte y dibujo",
    51 to "Clases de música",
    46 to "Clases particulares",
    8 to "Climatización",
    65 to "Cocina a domicilio",
    24 to "Community manager",
    43 to "Consultoría empresarial",
    84 to "Consultoría IT",
    31 to "Cuidado de mascotas",
    29 to "Cuidado de mayores",
    49 to "Cursos técnicos",
    14 to "Decoración",
    70 to "Decoración de interiores",
    76 to "Deportes acuáticos",
    21 to "Desarrollo móvil",
    20 to "Desarrollo web",
    15 to "Diseño gráfico",
    78 to "E-commerce",
    16 to "Edición de imágenes",
    18 to "Edición de vídeo",
    3 to "Electricidad",
    32 to "Entrenador personal",
    35 to "Estética y maquillaje",
    1 to "Ferretería", // ATENCIÓ: Abans era Abogacía, ara és Ferretería
    33 to "Fisioterapia",
    82 to "Floristería",
    2 to "Fontanería",
    48 to "Formación online",
    17 to "Fotografía",
    42 to "Gestión de proyectos",
    64 to "Grúa y asistencia",
    72 to "Guía turístico",
    50 to "Idiomas",
    58 to "Impresoras y periféricos",
    45 to "Ingeniería",
    56 to "Instalación de redes",
    9 to "Jardinería",
    81 to "Joyería",
    83 to "Librería",
    11 to "Limpieza",
    12 to "Limpieza industrial",
    71 to "Mantenimiento general",
    22 to "Marketing digital",
    36 to "Masajes",
    61 to "Mensajería",
    47 to "Mentoring",
    80 to "Moda y ropa",
    13 to "Mudanzas",
    28 to "Niñeras",
    37 to "Nutrición",
    68 to "Organización de eventos",
    67 to "Pastelería personalizada",
    34 to "Peluquería",
    4 to "Pintura",
    38 to "Psicología",
    41 to "Recursos humanos",
    26 to "Redacción y copywriting",
    7 to "Reformas del hogar",
    55 to "Reparación de electrodomésticos",
    54 to "Reparación de móviles",
    53 to "Reparación de ordenadors",
    57 to "Seguridad y CCTV",
    23 to "SEO / SEM",
    85 to "Servicios cloud",
    87 to "Soporte técnico",
    59 to "Taxi / VTC",
    77 to "Tienda minorista",
    25 to "Traducción",
    60 to "Transporte de mercancías",
    63 to "Transporte en bici / moto",
    69 to "Wedding planner"
    // OTHER es gestiona fora del mapa
)

// Objecte Helper per a la conversió entre ID, Nom i Enum.
object ServiceTypeLookup {
    // NOU FIX: Mapa Enum Name (String) -> ID (Int)
    // Utilitzem el nom de l'enum de Kotlin (p.ex., "ACTIVIDADES_AL_AIRE_LIBRE") com a clau.
    val enumToId: Map<String, Int> = SERVICE_TYPES.entries.associate { (id, name) ->
        // Apliquem la mateixa lògica de normalització per generar la clau de cerca,
        // assegurant que coincideixi amb la clau que es genera internament per l'enum
        // si els noms de l'enum són normalitzats i estan escrits igual que la clau de cerca.
        // Si no, la forma més segura és utilitzar el nom llegible (value) transformat a format ENUM

        // Mètode 1: Generar la clau a partir del nom llegible, que ha de coincidir amb ServiceType.name
        val enumKey = name.uppercase()
            .replace(" ", "_")
            .replace("/", "_")
            .replace("-", "_")
            .replace("Ñ", "N")
            .replace(
                "Á",
                "A"
            ) // Assegura't de cobrir tots els accents si els noms de l'enum són accentuats
            .replace("É", "E")
            .replace("Í", "I")
            .replace("Ó", "O")
            .replace("Ú", "U")

        // Aquesta clau generada ha de coincidir amb el valor de 'category.name'
        enumKey to id
    }

    // Aquesta funció auxiliar potser no és necessària si els noms de l'enum ja s'han normalitzat:
    fun nameToEnum(name: String): ServiceType? {
        val enumName =
            name.uppercase().replace(" ", "_").replace("/", "_").replace("-", "_").replace("Ñ", "N")
        return try {
            ServiceType.valueOf(enumName)
        } catch (e: Exception) {
            ServiceType.OTHER
        }
    }
}