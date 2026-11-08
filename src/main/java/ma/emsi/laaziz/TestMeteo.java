package ma.emsi.laaziz;

public class TestMeteo {

    public static void main(String[] args) {

       
        double latitude = 48.85;
        double longitude = 2.35;

        MeteoTool outil = new MeteoTool();

        System.out.println("=== TEST 7 : API OPEN-METEO ===");
        System.out.println("Ville : Paris");

        try {
            boolean pluiePrevue = outil.isPluiePrevue(
                    latitude,
                    longitude
            );

            if (pluiePrevue) {
                System.out.println(
                        "Conseil : prévoir un parapluie."
                );
            } else {
                System.out.println(
                        "Conseil : parapluie non nécessaire selon le seuil choisi."
                );
            }

        } catch (IllegalStateException e) {
            System.err.println(
                    "Erreur : " + e.getMessage()
            );
        }
    }
}