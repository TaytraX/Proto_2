package Core;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class ShaderManager {

    private final int programID;
    private int vertexShaderID, fragmentShaderID;

    private final Map<String, Integer> uniforms;

    public ShaderManager() throws Exception {
        programID = GL20.glCreateProgram();
        if(programID == 0) throw new Exception("Couldn't create shader !");
        uniforms = new HashMap<>();
    }

    public void createUniform(String uniformName) throws Exception {
        int uniformLocation = GL20.glGetUniformLocation(programID, uniformName);
        if(uniformLocation < 0) {
            // 🔧 FIX 1: Warning au lieu d'exception pour les uniforms optionnels
            System.out.println("⚠️ Uniform '" + uniformName + "' non trouvé dans le shader (peut être optimisé par le compilateur)");
            // Pour les uniforms critiques, vous pouvez décommenter la ligne suivante :
            // throw new Exception("Couldn't find uniform " + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    // 🔧 FIX 2: Vérification de l'existence de l'uniform avant utilisation
    private boolean hasUniform(String uniformName) {
        Integer location = uniforms.get(uniformName);
        return location != null && location >= 0;
    }

    public void setUniforms(String uniformName, Matrix4f value) {
        if (!hasUniform(uniformName)) {
            System.out.println("⚠️ Tentative d'utilisation d'uniform inexistant: " + uniformName);
            return;
        }
        try(MemoryStack stack = MemoryStack.stackPush()) {
            GL20.glUniformMatrix4fv(uniforms.get(uniformName), false, value.get(stack.mallocFloat(16)));
        }
    }

    public void setUniform(String uniformName, float value) {
        if (!hasUniform(uniformName)) {
            System.out.println("⚠️ Tentative d'utilisation d'uniform inexistant: " + uniformName);
            return;
        }
        GL20.glUniform1f(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, float x, float y) {
        if (!hasUniform(uniformName)) {
            System.out.println("⚠️ Tentative d'utilisation d'uniform inexistant: " + uniformName);
            return;
        }
        GL20.glUniform2f(uniforms.get(uniformName), x, y);
    }

    // 🔧 FIX 3: Correction du nom du paramètre (width -> value)
    public void setUniform(String uniformName, int value) {
        if (!hasUniform(uniformName)) {
            System.out.println("⚠️ Tentative d'utilisation d'uniform inexistant: " + uniformName);
            return;
        }
        GL20.glUniform1i(uniforms.get(uniformName), value);
    }

    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderID = createShader(shaderCode, GL20.GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderID = createShader(shaderCode, GL20.GL_FRAGMENT_SHADER);
    }

    public int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderID = GL20.glCreateShader(shaderType);

        if(shaderID == 0) throw new Exception("Error creating shader. Type : " + shaderType);

        GL20.glShaderSource(shaderID, shaderCode);
        GL20.glCompileShader(shaderID);

        if(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == 0) {
            String error = GL20.glGetShaderInfoLog(shaderID, 1024);
            System.err.println("❌ Erreur compilation shader:");
            System.err.println("Type: " + (shaderType == GL20.GL_VERTEX_SHADER ? "VERTEX" : "FRAGMENT"));
            System.err.println("Erreur: " + error);
            System.err.println("Code du shader:");
            System.err.println(shaderCode);
            throw new Exception("Error compiling shader code : Type: " + shaderType + " Info " + error);
        }

        GL20.glAttachShader(programID, shaderID);
        return shaderID;
    }

    public void link() throws Exception {
        GL20.glLinkProgram(programID);

        if(GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == 0) {
            String error = GL20.glGetProgramInfoLog(programID, 1024);
            System.err.println("❌ Erreur de liaison du programme shader:");
            System.err.println(error);
            throw new Exception("Error linking shader code Info " + error);
        }

        // 🔧 FIX 4: Détacher et supprimer les shaders après la liaison
        if(vertexShaderID != 0) {
            GL20.glDetachShader(programID, vertexShaderID);
            GL20.glDeleteShader(vertexShaderID);
        }

        if(fragmentShaderID != 0) {
            GL20.glDetachShader(programID, fragmentShaderID);
            GL20.glDeleteShader(fragmentShaderID);
        }

        GL20.glValidateProgram(programID);
        if(GL20.glGetProgrami(programID, GL20.GL_VALIDATE_STATUS) == 0) {
            String error = GL20.glGetProgramInfoLog(programID, 1024);
            System.err.println("❌ Erreur de validation du programme shader:");
            System.err.println(error);
            throw new Exception("Unable to validate shader code: " + error);
        }

        System.out.println("✅ Shader programme lié et validé avec succès");
    }

    public int getUniformLocation(String uniformName) {
        return GL20.glGetUniformLocation(programID, uniformName);
    }

    public void setUniform(String uniformName, Matrix4f matrix) {
        if (!hasUniform(uniformName)) {
            System.out.println("⚠️ Tentative d'utilisation d'uniform inexistant: " + uniformName);
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            GL20.glUniformMatrix4fv(getUniformLocation(uniformName), false, buffer);
        }
    }

    public void bind() {
        GL20.glUseProgram(programID);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if(programID != 0) {
            GL20.glDeleteProgram(programID);
        }
    }

    public int getProgramID() {
        return programID;
    }
}