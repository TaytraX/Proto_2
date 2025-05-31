#version 400 core

in vec3 position;
out vec3 colour;

void main() {
    gl_Position = vec4(position, 1.0);  // P MAJUSCULE !
    colour = vec3(position.x + 0.5, 0.5, position.y + 0.5);
}
