#version 330

out vec4 fragColor;

uniform float time;
uniform vec2 resolution;

void main() {
    // Fond bleu nuit simple
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 color = vec3(0.02, 0.02, 0.1);

    // Ajout d'un point blanc au centre pour tester
    if (length(uv - 0.5) < 0.1) {
        color = vec3(1.0);
    }

    fragColor = vec4(color, 1.0);
}