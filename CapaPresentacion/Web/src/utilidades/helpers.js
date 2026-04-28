// Utilidades y helpers para la aplicación
import { formatearFecha, formatearHora, formatearFechaRelativa } from './formateoFechas'

/**
 * Valida un email
 */
export function validarEmail(email) {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return regex.test(email)
}

/**
 * Valida una contraseña
 * Mínimo 8 caracteres, al menos una mayúscula, una minúscula y un número
 */
export function validarContraseña(contraseña) {
  const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/
  return regex.test(contraseña)
}

/**
 * Trunca un texto a una longitud máxima
 */
export function truncarTexto(texto, longitud = 50) {
  if (!texto) return ''
  return texto.length > longitud ? texto.substring(0, longitud) + '...' : texto
}

/**
 * Obtiene las iniciales de un nombre
 */
export function obtenerIniciales(nombre) {
  if (!nombre) return '?'
  return nombre
    .split(' ')
    .map(parte => parte[0])
    .join('')
    .toUpperCase()
    .substring(0, 2)
}

/**
 * Genera un color aleatorio basado en un string
 */
export function generarColorDelNombre(nombre) {
  const colores = [
    '#667eea',
    '#764ba2',
    '#f093fb',
    '#4facfe',
    '#00f2fe',
    '#43e97b',
    '#fa709a',
    '#fee140'
  ]

  let hash = 0
  for (let i = 0; i < nombre.length; i++) {
    hash = nombre.charCodeAt(i) + ((hash << 5) - hash)
  }

  return colores[Math.abs(hash) % colores.length]
}

/**
 * Copia un texto al portapapeles
 */
export async function copiarAlPortapapeles(texto) {
  try {
    await navigator.clipboard.writeText(texto)
    return true
  } catch (error) {
    console.error('Error al copiar:', error)
    return false
  }
}

/**
 * Obtiene un valor del localStorage de forma segura
 */
export function obtenerDelStorage(clave, porDefecto = null) {
  try {
    const valor = localStorage.getItem(clave)
    return valor ? JSON.parse(valor) : porDefecto
  } catch (error) {
    console.error(`Error al obtener ${clave} del storage:`, error)
    return porDefecto
  }
}

/**
 * Guarda un valor en localStorage de forma segura
 */
export function guardarEnStorage(clave, valor) {
  try {
    localStorage.setItem(clave, JSON.stringify(valor))
    return true
  } catch (error) {
    console.error(`Error al guardar ${clave} en storage:`, error)
    return false
  }
}

/**
 * Elimina un valor del localStorage
 */
export function eliminarDelStorage(clave) {
  try {
    localStorage.removeItem(clave)
    return true
  } catch (error) {
    console.error(`Error al eliminar ${clave} del storage:`, error)
    return false
  }
}

/**
 * Valida que dos contraseñas coincidan
 */
export function contraseñasCoinciden(contraseña1, contraseña2) {
  return contraseña1 === contraseña2 && contraseña1.length > 0
}

/**
 * Obtiene una URL de avatar del usuario usando Gravatar
 */
export function obtenerUrlAvatar(email) {
  const hashGravatar = (str) => {
    let hash = 0
    for (let i = 0; i < str.length; i++) {
      hash = ((hash << 5) - hash) + str.charCodeAt(i)
      hash = hash & hash
    }
    return Math.abs(hash).toString(16)
  }

  const emailLower = (email || '').toLowerCase().trim()
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(emailLower)}&background=667eea&color=fff`
}

/**
 * Obtiene el nombre visible de una conversación
 * Para chats privados, devuelve solo el nombre del otro participante
 * Para grupos, devuelve el nombre del grupo
 */
export function obtenerNombreVisibleConversacion(conversacion, usuarioActualId) {
  if (!conversacion) return 'Chat'
  
  // Si es un grupo, devolver el nombre del grupo
  if (conversacion.tipo === 'GRUPO') {
    return conversacion.nombre
  }
  
  // Si es privado y tenemos el ID del usuario actual, extraer el nombre del otro
  if (conversacion.tipo === 'PRIVADA' && usuarioActualId && conversacion.participantes) {
    const otro = conversacion.participantes.find(p => p.id !== usuarioActualId)
    if (otro && otro.username) {
      return otro.username
    }
  }
  
  // Fallback: devolver el nombre de la conversación
  return conversacion.nombre || 'Chat'
}

export { formatearFecha, formatearHora, formatearFechaRelativa }
