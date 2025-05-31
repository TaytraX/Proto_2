#version 400 core

in vec3 position;
in vec2 textureCoord;

out vec2 fragTextureCoord;

uniform mat4 transformationMatrix; // ✅ NOUVEAU: Matrice de transformation

void main() {
    // ✅ NOUVEAU: Appliquer la transformation à la position
    gl_Position = transformationMatrix * vec4(position, 1.0);
    fragTextureCoord = textureCoord;
}