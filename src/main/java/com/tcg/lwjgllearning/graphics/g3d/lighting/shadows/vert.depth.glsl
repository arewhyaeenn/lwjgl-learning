precision mediump float;

uniform mat4 modelToShadowspace;
attribute vec3 vertPosition;

void main()
{
    gl_Position = modelToShadowspace * vec4(vertPosition, 1.0);
}