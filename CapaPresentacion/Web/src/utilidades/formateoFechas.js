// Funciones para formatear fechas
// Utiliza el locale español (es-ES)

/**
 * Formatea una fecha completa (día, mes, año)
 * Ejemplo: "16 de abril de 2026"
 */
export function formatearFecha(fecha) {
  if (!fecha) return ''

  const date = new Date(fecha)
  return date.toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

/**
 * Formatea solo la hora
 * Ejemplo: "23:52"
 */
export function formatearHora(fecha) {
  if (!fecha) return ''

  const date = new Date(fecha)
  return date.toLocaleTimeString('es-ES', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * Formatea fecha y hora juntas
 * Ejemplo: "16 de abril de 2026 a las 23:52"
 */
export function formatearFechaYHora(fecha) {
  if (!fecha) return ''

  const date = new Date(fecha)
  return date.toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * Formatea fecha de manera relativa
 * Ejemplo: "hace 5 minutos", "hace 2 horas", "hace 3 días"
 */
export function formatearFechaRelativa(fecha) {
  if (!fecha) return ''

  const date = new Date(fecha)
  const ahora = new Date()
  const diferencia = ahora - date

  // Milisegundos en diferentes unidades
  const minuto = 60 * 1000
  const hora = 60 * minuto
  const dia = 24 * hora
  const semana = 7 * dia
  const mes = 30 * dia
  const año = 365 * dia

  if (diferencia < minuto) {
    return 'ahora'
  } else if (diferencia < hora) {
    const minutos = Math.floor(diferencia / minuto)
    return `hace ${minutos} ${minutos === 1 ? 'minuto' : 'minutos'}`
  } else if (diferencia < dia) {
    const horas = Math.floor(diferencia / hora)
    return `hace ${horas} ${horas === 1 ? 'hora' : 'horas'}`
  } else if (diferencia < semana) {
    const dias = Math.floor(diferencia / dia)
    return `hace ${dias} ${dias === 1 ? 'día' : 'días'}`
  } else if (diferencia < mes) {
    const semanas = Math.floor(diferencia / semana)
    return `hace ${semanas} ${semanas === 1 ? 'semana' : 'semanas'}`
  } else if (diferencia < año) {
    const meses = Math.floor(diferencia / mes)
    return `hace ${meses} ${meses === 1 ? 'mes' : 'meses'}`
  } else {
    const años = Math.floor(diferencia / año)
    return `hace ${años} ${años === 1 ? 'año' : 'años'}`
  }
}

/**
 * Formatea solo el día de la semana
 * Ejemplo: "lunes", "martes", etc.
 */
export function obtenerDiaSemana(fecha) {
  if (!fecha) return ''

  const date = new Date(fecha)
  return date.toLocaleDateString('es-ES', { weekday: 'long' })
}

/**
 * Verifica si la fecha es de hoy
 */
export function esHoy(fecha) {
  if (!fecha) return false

  const date = new Date(fecha)
  const hoy = new Date()

  return (
    date.getDate() === hoy.getDate() &&
    date.getMonth() === hoy.getMonth() &&
    date.getFullYear() === hoy.getFullYear()
  )
}

/**
 * Verifica si la fecha es de ayer
 */
export function esAyer(fecha) {
  if (!fecha) return false

  const date = new Date(fecha)
  const ayer = new Date()
  ayer.setDate(ayer.getDate() - 1)

  return (
    date.getDate() === ayer.getDate() &&
    date.getMonth() === ayer.getMonth() &&
    date.getFullYear() === ayer.getFullYear()
  )
}

/**
 * Formatea la hora de forma inteligente
 * Si es de hoy: "23:52"
 * Si es de ayer: "Ayer 23:52"
 * Si es de esta semana: "lunes 23:52"
 * Si no: "16 de abril de 2026"
 */
export function formatearFechaInteligente(fecha) {
  if (!fecha) return ''

  if (esHoy(fecha)) {
    return formatearHora(fecha)
  } else if (esAyer(fecha)) {
    return `Ayer ${formatearHora(fecha)}`
  } else if (new Date() - new Date(fecha) < 7 * 24 * 60 * 60 * 1000) {
    return `${obtenerDiaSemana(fecha)} ${formatearHora(fecha)}`
  } else {
    return formatearFecha(fecha)
  }
}
