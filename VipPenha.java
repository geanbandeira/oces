
public class VipPenha {
    static class Result {
        double[] x;
        double z;
        double[] slack;
        Result(double[] x, double z, double[] slack) {
            this.x = x; this.z = z; this.slack = slack;
        }
    }

    static Result solve(double[][] A, double[] b, double[] c) {
        int m = b.length, n = c.length;
        int cols = n + m + 1;
        double[][] t = new double[m + 1][cols];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) t[i][j] = A[i][j];
            t[i][n + i] = 1.0;
            t[i][cols - 1] = b[i];
        }
        for (int j = 0; j < n; j++) t[m][j] = -c[j];

        int maxIterations = 1000;
        for (int iter = 0; iter < maxIterations; iter++) {
            int entering = -1;
            double mostNegative = -1e-9;
            for (int j = 0; j < cols - 1; j++) {
                if (t[m][j] < mostNegative) {
                    mostNegative = t[m][j];
                    entering = j;
                }
            }
            if (entering == -1) break;

            int leaving = -1;
            double bestRatio = Double.POSITIVE_INFINITY;
            for (int i = 0; i < m; i++) {
                if (t[i][entering] > 1e-9) {
                    double ratio = t[i][cols - 1] / t[i][entering];
                    if (ratio < bestRatio) {
                        bestRatio = ratio;
                        leaving = i;
                    }
                }
            }
            if (leaving == -1) throw new IllegalStateException("Modelo ilimitado.");

            double pivot = t[leaving][entering];
            for (int j = 0; j < cols; j++) t[leaving][j] /= pivot;
            for (int i = 0; i <= m; i++) {
                if (i == leaving) continue;
                double factor = t[i][entering];
                for (int j = 0; j < cols; j++)
                    t[i][j] -= factor * t[leaving][j];
            }
            if (iter == maxIterations - 1)
                throw new IllegalStateException("Limite de iteracoes atingido.");
        }

        double[] x = new double[n];
        for (int j = 0; j < n; j++) {
            int basicRow = -1;
            for (int i = 0; i < m; i++) {
                if (Math.abs(t[i][j] - 1.0) < 1e-8) {
                    boolean unit = true;
                    for (int k = 0; k < m; k++)
                        if (k != i && Math.abs(t[k][j]) > 1e-8) unit = false;
                    if (unit) { basicRow = i; break; }
                }
            }
            if (basicRow != -1) x[j] = t[basicRow][cols - 1];
        }

        double z = 0;
        for (int j = 0; j < n; j++) z += c[j] * x[j];
        double[] slack = new double[m];
        for (int i = 0; i < m; i++) {
            slack[i] = b[i];
            for (int j = 0; j < n; j++) slack[i] -= A[i][j] * x[j];
        }
        return new Result(x, z, slack);
    }

    static void runScenario(String name, double onboardingHours, double supportHours) {
        double[][] A = {
            {2.0, 4.0},
            {1.0, 1.5},
            {1.0, 1.0}
        };
        double[] b = {onboardingHours, supportHours, 50.0};
        double[] c = {300.0, 500.0};
        Result r = solve(A, b, c);

        System.out.println("\n" + name);
        System.out.printf("Plano Essencial (x1): %.2f clientes/mês%n", r.x[0]);
        System.out.printf("Plano Premium (x2):   %.2f clientes/mês%n", r.x[1]);
        System.out.printf("Contribuição estimada: R$ %.2f/mês%n", r.z);
        String[] labels = {"Implantação", "Suporte", "Capacidade comercial"};
        for (int i = 0; i < r.slack.length; i++)
            System.out.printf("Folga: %s: %.2f unidades%n", labels[i], r.slack[i]);
    }

    public static void main(String[] args) {
        runScenario("CENÁRIO A: recursos planejados", 160.0, 90.0);
        runScenario("\nCENÁRIO B: redução de 25% nas horas de implantação", 120.0, 90.0);
    }
}
