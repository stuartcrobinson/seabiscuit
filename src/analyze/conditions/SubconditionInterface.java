package analyze.conditions;


interface SubconditionInterface {
    public boolean isMet(float x);

    public boolean isMet(byte x);

    public boolean isMet(int x);

    public boolean isMet(String x);

    public boolean isMet(boolean x);

    public String getOutputString();
}
