#version 330 core

out vec4 fragColor;

uniform float time;
uniform vec2 resolution;

void main() {
    // ✅ CORRECTION: Gérer le cas où les uniforms ne sont pas disponibles
    vec2 uv = gl_FragCoord.xy;

    // Si resolution est disponible, normaliser les coordonnées
    if (resolution.x > 0.0 && resolution.y > 0.0) {
        uv = gl_FragCoord.xy / resolution.xy;
    } else {
        // Fallback: utiliser les coordonnées OpenGL directement
        uv = (gl_FragCoord.xy / 800.0); // Valeur par défaut
    }

    // Ciel dégradé bleu avec animation basée sur le temps
    float skyBlue = 0.3 + 0.2 * sin(time * 0.5);
    float lightBlue = 0.7 + 0.3 * cos(time * 0.3);

    // Dégradé vertical du bleu foncé (bas) au bleu clair (haut)
    vec3 topColor = vec3(0.5, 0.8, 1.0) * lightBlue;
    vec3 bottomColor = vec3(0.2, 0.4, 0.8) * skyBlue;

    vec3 skyColor = mix(bottomColor, topColor, uv.y);

    // Ajouter quelques nuages simples
    float cloud1 = sin(uv.x * 10.0 + time) * sin(uv.y * 6.0) * 0.1;
    float cloud2 = cos(uv.x * 8.0 - time * 0.7) * cos(uv.y * 4.0 + time * 0.3) * 0.05;

    skyColor += vec3(1.0, 1.0, 1.0) * max(0.0, cloud1 + cloud2);

    fragColor = vec4(skyColor, 1.0);
}