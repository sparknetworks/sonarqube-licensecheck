package at.porscheinformatik.sonarqube.licensecheck.model;

public enum Operator {
    AND, OR;

    public CharSequence delimiter() {
        return " " + name() + " ";
    }
}
