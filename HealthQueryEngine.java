import java.time.LocalDate;
import java.util.*;

/*
 * ============================================================
 * HEALTH QUERY - MEDICAL PATIENT SEARCH & DSA SYSTEM
 * ============================================================
 *
 * Single-file Java implementation of the uploaded project.
 *
 * CO-WISE DSA COVERAGE USED IN THIS PROJECT:
 * CO1 - Algorithmic problem classification, algorithm selection and complexity evaluation
 * CO2 - String Matching: Naive Matching, KMP, Rabin-Karp and hashing
 * CO3 - Dynamic Programming, Edit Distance and Wagner-Fischer style DP
 * CO4 - Flow Networks, Ford-Fulkerson / Edmonds-Karp maximum flow
 *
 * Compile:
 *     javac HealthQueryEngine.java
 *
 * Run:
 *     java HealthQueryEngine
 * ============================================================
 */

public class HealthQueryEngine {

    // ============================================================
    // ENUMS
    // ============================================================

    enum Gender {
        MALE, FEMALE, OTHER
    }

    enum Status {
        INPATIENT,
        OUTPATIENT,
        DISCHARGED,
        CRITICAL
    }

    enum Category {
        DIAGNOSIS,
        DRUG,
        SYMPTOM
    }

    enum ErrorType {
        OMISSION,
        INSERTION,
        SUBSTITUTION,
        TRANSPOSITION,
        EXACT_MATCH
    }

    // ============================================================
    // PATIENT MEDICATION
    // ============================================================

    static class PatientMedication {
        String drugName;
        String dosage;
        String frequency;

        PatientMedication(String drugName, String dosage, String frequency) {
            this.drugName = drugName;
            this.dosage = dosage;
            this.frequency = frequency;
        }

        @Override
        public String toString() {
            return drugName + " | " + dosage + " | " + frequency;
        }
    }

    // ============================================================
    // PATIENT CLASS
    // ============================================================

    static class Patient {
        String id;
        String name;
        int age;
        Gender gender;
        String bloodGroup;
        String contact;
        String admissionDate;
        String diagnosis;
        List<String> symptoms;
        List<PatientMedication> medications;
        String attendingPhysician;
        String department;
        Status status;
        String notes;

        Patient(
                String id,
                String name,
                int age,
                Gender gender,
                String bloodGroup,
                String contact,
                String admissionDate,
                String diagnosis,
                List<String> symptoms,
                List<PatientMedication> medications,
                String attendingPhysician,
                String department,
                Status status,
                String notes
        ) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.bloodGroup = bloodGroup;
            this.contact = contact;
            this.admissionDate = admissionDate;
            this.diagnosis = diagnosis;
            this.symptoms = symptoms;
            this.medications = medications;
            this.attendingPhysician = attendingPhysician;
            this.department = department;
            this.status = status;
            this.notes = notes;
        }

        @Override
        public String toString() {
            return id + " | " +
                    name + " | Age: " +
                    age + " | " +
                    diagnosis + " | " +
                    department + " | " +
                    status;
        }
    }

    // ============================================================
    // MEDICAL DICTIONARY ENTRY
    // ============================================================

    static class MedicalEntry {
        String id;
        String term;
        Category category;
        String categoryLabel;
        String definition;
        String indications;
        String clinicalNotes;
        List<String> commonMisspellings;

        MedicalEntry(
                String id,
                String term,
                Category category,
                String categoryLabel,
                String definition,
                String indications,
                String clinicalNotes,
                String... commonMisspellings
        ) {
            this.id = id;
            this.term = term;
            this.category = category;
            this.categoryLabel = categoryLabel;
            this.definition = definition;
            this.indications = indications;
            this.clinicalNotes = clinicalNotes;
            this.commonMisspellings =
                    new ArrayList<>(Arrays.asList(commonMisspellings));
        }
    }

    // ============================================================
    // SPELLING DETECTION RESULT
    // ============================================================

    static class SpellingMistakeDetection {
        String originalQuery;
        String correctedTerm;
        Category category;
        String categoryLabel;
        int editDistance;
        int similarity;
        ErrorType errorType;
        String explanation;
        MedicalEntry matchedEntry;
        String matchedWord;

        @Override
        public String toString() {
            return "\nOriginal Query : " + originalQuery +
                    "\nCorrected Term : " + correctedTerm +
                    "\nCategory       : " + category +
                    "\nDistance       : " + editDistance +
                    "\nSimilarity     : " + similarity + "%" +
                    "\nError Type     : " + errorType +
                    "\nExplanation    : " + explanation;
        }
    }

    // ============================================================
    // SEARCH RESULT
    // ============================================================

    static class PatientSearchResult {
        Patient patient;
        int distance;
        int similarity;

        PatientSearchResult(Patient patient, int distance, int similarity) {
            this.patient = patient;
            this.distance = distance;
            this.similarity = similarity;
        }
    }

    // ============================================================
    // DIAGNOSIS TEMPLATE
    // ============================================================

    static class DiagnosisTemplate {
        String baseDiagnosis;
        String department;
        String[] subtypes;
        String[] symptoms;
        MedicationTemplate[] medications;

        DiagnosisTemplate(
                String baseDiagnosis,
                String department,
                String[] subtypes,
                String[] symptoms,
                MedicationTemplate[] medications
        ) {
            this.baseDiagnosis = baseDiagnosis;
            this.department = department;
            this.subtypes = subtypes;
            this.symptoms = symptoms;
            this.medications = medications;
        }
    }

    // ============================================================
    // MEDICATION TEMPLATE
    // ============================================================

    static class MedicationTemplate {
        String drugName;
        String[] dosages;
        String[] frequencies;

        MedicationTemplate(
                String drugName,
                String[] dosages,
                String[] frequencies
        ) {
            this.drugName = drugName;
            this.dosages = dosages;
            this.frequencies = frequencies;
        }
    }

    // ============================================================
    // BENCHMARK TEST CASE
    // ============================================================

    static class BenchmarkTestCase {
        String query;
        String target;
        Category category;
        ErrorType expectedError;
        int expectedDistance;

        BenchmarkTestCase(
                String query,
                String target,
                Category category,
                ErrorType expectedError,
                int expectedDistance
        ) {
            this.query = query;
            this.target = target;
            this.category = category;
            this.expectedError = expectedError;
            this.expectedDistance = expectedDistance;
        }
    }

    // ============================================================
    // GLOBAL DATA
    // ============================================================

    static final Scanner scanner = new Scanner(System.in);

    static List<Patient> patients = new ArrayList<>();

    static final List<MedicalEntry> MEDICAL_CORPUS =
            new ArrayList<>();

    static final String[] FIRST_NAMES = {
            "Rahul", "Sneha", "Arjun", "Priya", "Kiran",
            "Ananya", "Ravi", "Neha", "Vikram", "Meena",
            "Aditya", "Divya", "Rohan", "Kavya", "Suresh",
            "Pooja", "Amit", "Lakshmi", "Nikhil", "Swathi"
    };

    static final String[] LAST_NAMES = {
            "Rao", "Sharma", "Gupta", "Reddy", "Patel",
            "Kumar", "Naidu", "Iyer", "Joshi", "Verma",
            "Chowdary", "Mehta", "Das", "Singh", "Nair",
            "Varma", "Menon", "Deshmukh", "Kulkarni", "Mishra"
    };

    static final String[] MIDDLE_INITIALS = {
            "A.", "B.", "C.", "D.", "K.",
            "M.", "P.", "R.", "S.", "V."
    };

    static final String[] BLOOD_GROUPS = {
            "O+", "B+", "A+", "AB+",
            "O-", "B-", "A-", "AB-"
    };

    static final String[] PHYSICIANS = {
            "Dr. A. V. Rao",
            "Dr. S. K. Gupta",
            "Dr. Meenakshi Sundaram",
            "Dr. K. V. Sharma",
            "Dr. Priya Radhakrishnan",
            "Dr. R. C. Chowdary",
            "Dr. Anita Joshi",
            "Dr. Vikram Patel",
            "Dr. Sunita Deshmukh",
            "Dr. Rajeshwar Naidu",
            "Dr. Vandana Saxena",
            "Dr. Hemanth Kumar",
            "Dr. Bhavana Reddy",
            "Dr. Chetan Kulkarni",
            "Dr. Shalini Iyer",
            "Dr. Naveen Menon"
    };

    // ============================================================
    // MAIN
    // ============================================================

    public static void main(String[] args) {

        initializeMedicalCorpus();

        patients = generate1000Patients();

        printHeader();

        while (true) {

            printMainMenu();

            int choice = readInt("Enter your choice: ");

            switch (choice) {

                case 1:
                    patientDirectory();
                    break;

                case 2:
                    searchPatientsMenu();
                    break;

                case 3:
                    medicalDictionary();
                    break;

                case 4:
                    spellingCorrectionMenu();
                    break;

                case 5:
                    displayDPMatixMenu();
                    break;

                case 6:
                    exactVsFuzzyComparison();
                    break;

                case 7:
                    benchmarkSystem();
                    break;

                case 8:
                    showStatistics();
                    break;

                case 9:
                    addPatient();
                    break;


                case 0:
                    System.out.println("\nExiting Health Query System...");
                    System.out.println("Thank you.");
                    return;

                default:
                    System.out.println("\nInvalid choice.");
            }
        }
    }

    // ============================================================
    // HEADER
    // ============================================================

    static void printHeader() {

        System.out.println();
        System.out.println("============================================================");
        System.out.println("       HEALTH QUERY - MEDICAL DSA SEARCH SYSTEM");
        System.out.println("============================================================");
        System.out.println("   Levenshtein Distance | Fuzzy Search | DP | Dictionary");
        System.out.println("============================================================");
    }

    // ============================================================
    // MAIN MENU
    // ============================================================

    static void printMainMenu() {

        System.out.println("\n-------------------- MAIN MENU --------------------");
        System.out.println("1. Patient Directory");
        System.out.println("2. Search Patients");
        System.out.println("3. Medical Dictionary / Corpus");
        System.out.println("4. Spelling Correction");
        System.out.println("5. Dynamic Programming Matrix");
        System.out.println("6. Exact vs Fuzzy Comparison");
        System.out.println("7. Benchmark Evaluation");
        System.out.println("8. Database Statistics");
        System.out.println("9. Add Patient");
        System.out.println("0. Exit");
        System.out.println("---------------------------------------------------");
    }

    // ============================================================
    // PATIENT DIRECTORY
    // ============================================================

    static void patientDirectory() {

        while (true) {

            System.out.println("\n================ PATIENT DIRECTORY ================");
            System.out.println("Total Records: " + patients.size());

            System.out.println("1. Show first 20 patients");
            System.out.println("2. Show patient details");
            System.out.println("3. Search patient");
            System.out.println("4. Filter by department");
            System.out.println("5. Filter by status");
            System.out.println("6. Back");

            int choice = readInt("Choice: ");

            switch (choice) {

                case 1:
                    displayPatients(patients.subList(
                            0,
                            Math.min(20, patients.size())
                    ));
                    break;

                case 2:
                    String id = readLine("Enter Patient ID: ");
                    Patient p = findPatientById(id);

                    if (p != null) {
                        displayPatientDetails(p);
                    } else {
                        System.out.println("Patient not found.");
                    }
                    break;

                case 3:
                    searchPatientsMenu();
                    break;

                case 4:
                    String dept = readLine("Department: ");
                    List<Patient> deptPatients =
                            filterByDepartment(dept);
                    displayPatients(deptPatients);
                    break;

                case 5:
                    String status = readLine(
                            "Status (INPATIENT/OUTPATIENT/DISCHARGED/CRITICAL): "
                    );

                    List<Patient> statusPatients =
                            filterByStatus(status);

                    displayPatients(statusPatients);
                    break;

                case 6:
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ============================================================
    // DISPLAY PATIENTS
    // ============================================================

    static void displayPatients(List<Patient> list) {

        if (list.isEmpty()) {
            System.out.println("\nNo patients found.");
            return;
        }

        System.out.println("\n------------------------------------------------------------");

        int count = 0;

        for (Patient p : list) {

            System.out.println(
                    p.id + " | " +
                    p.name + " | Age " +
                    p.age + " | " +
                    p.gender + " | " +
                    p.diagnosis + " | " +
                    p.department + " | " +
                    p.status
            );

            count++;

            if (count >= 50) {
                System.out.println(
                        "\nShowing first 50 results."
                );
                break;
            }
        }

        System.out.println("------------------------------------------------------------");
        System.out.println("Results: " + list.size());
    }

    // ============================================================
    // PATIENT DETAILS
    // ============================================================

    static void displayPatientDetails(Patient p) {

        System.out.println("\n============================================================");
        System.out.println("                    PATIENT DETAILS");
        System.out.println("============================================================");

        System.out.println("Patient ID          : " + p.id);
        System.out.println("Name                : " + p.name);
        System.out.println("Age                 : " + p.age);
        System.out.println("Gender              : " + p.gender);
        System.out.println("Blood Group         : " + p.bloodGroup);
        System.out.println("Contact             : " + p.contact);
        System.out.println("Admission Date      : " + p.admissionDate);
        System.out.println("Diagnosis           : " + p.diagnosis);
        System.out.println("Department          : " + p.department);
        System.out.println("Status              : " + p.status);
        System.out.println("Attending Physician : " + p.attendingPhysician);

        System.out.println("\nSymptoms:");

        for (String s : p.symptoms) {
            System.out.println("  - " + s);
        }

        System.out.println("\nMedications:");

        for (PatientMedication m : p.medications) {
            System.out.println("  - " + m);
        }

        System.out.println("\nClinical Notes:");
        System.out.println(p.notes);

        System.out.println("============================================================");
    }

    // ============================================================
    // SEARCH PATIENT MENU
    // ============================================================

    static void searchPatientsMenu() {

        System.out.println("\n================ PATIENT SEARCH =================");

        String query = readLine(
                "Enter name / ID / diagnosis / symptom / drug / department: "
        );

        int maxDistance = readInt(
                "Maximum edit distance (recommended 2): "
        );

        List<PatientSearchResult> results =
                searchPatients(query, maxDistance);

        if (results.isEmpty()) {

            System.out.println("\nNo matching patients.");

            // Try dictionary correction
            SpellingMistakeDetection mistake =
                    detectAndCorrectMedicalMistake(query, null);

            if (mistake != null &&
                    mistake.editDistance > 0) {

                System.out.println(
                        "\nDid you mean: " +
                        mistake.correctedTerm + "?"
                );

                System.out.println(mistake);
            }

            return;
        }

        results.sort(
                Comparator.comparingInt(
                        r -> r.distance
                )
        );

        System.out.println("\n================ SEARCH RESULTS ================");

        int pageSize = 10;
        int totalPages =
                (results.size() + pageSize - 1) / pageSize;

        int currentPage = 1;

        while (true) {

            int start =
                    (currentPage - 1) * pageSize;

            int end =
                    Math.min(
                            start + pageSize,
                            results.size()
                    );

            System.out.println(
                    "\nPage " +
                    currentPage +
                    " / " +
                    totalPages
            );

            for (int i = start; i < end; i++) {

                PatientSearchResult r = results.get(i);

                System.out.println(
                        r.patient.id +
                        " | " +
                        r.patient.name +
                        " | " +
                        r.patient.diagnosis +
                        " | Distance: " +
                        r.distance +
                        " | Similarity: " +
                        r.similarity +
                        "%"
                );
            }

            System.out.println(
                    "\nN = Next | P = Previous | Q = Quit"
            );

            String cmd =
                    readLine("Command: ");

            if (cmd.equalsIgnoreCase("N")) {

                if (currentPage < totalPages) {
                    currentPage++;
                }

            } else if (cmd.equalsIgnoreCase("P")) {

                if (currentPage > 1) {
                    currentPage--;
                }

            } else if (cmd.equalsIgnoreCase("Q")) {
                break;
            }
        }
    }

    // ============================================================
    // SEARCH PATIENTS
    // ============================================================

    static List<PatientSearchResult> searchPatients(
            String query,
            int maxDistance
    ) {

        List<PatientSearchResult> results =
                new ArrayList<>();

        query = query.trim().toLowerCase();

        if (query.isEmpty()) {
            return results;
        }

        for (Patient p : patients) {

            int bestDistance =
                    Integer.MAX_VALUE;

            // Name
            bestDistance = Math.min(
                    bestDistance,
                    fuzzyFieldDistance(
                            query,
                            p.name
                    )
            );

            // ID
            bestDistance = Math.min(
                    bestDistance,
                    fuzzyFieldDistance(
                            query,
                            p.id
                    )
            );

            // Diagnosis
            bestDistance = Math.min(
                    bestDistance,
                    fuzzyFieldDistance(
                            query,
                            p.diagnosis
                    )
            );

            // Department
            bestDistance = Math.min(
                    bestDistance,
                    fuzzyFieldDistance(
                            query,
                            p.department
                    )
            );

            // Symptoms
            for (String symptom : p.symptoms) {

                bestDistance = Math.min(
                        bestDistance,
                        fuzzyFieldDistance(
                                query,
                                symptom
                        )
                );
            }

            // Medication
            for (PatientMedication med : p.medications) {

                bestDistance = Math.min(
                        bestDistance,
                        fuzzyFieldDistance(
                                query,
                                med.drugName
                        )
                );
            }

            // Exact substring match gets distance 0
            if (
                    p.name.toLowerCase().contains(query) ||
                    p.id.toLowerCase().contains(query) ||
                    p.diagnosis.toLowerCase().contains(query) ||
                    p.department.toLowerCase().contains(query)
            ) {
                bestDistance = 0;
            }

            if (bestDistance <= maxDistance) {

                int maxLength =
                        Math.max(
                                query.length(),
                                bestDistance == 0
                                        ? query.length()
                                        : query.length() + bestDistance
                        );

                int similarity =
                        Math.max(
                                0,
                                100 -
                                (bestDistance * 100 /
                                Math.max(1, maxLength))
                        );

                results.add(
                        new PatientSearchResult(
                                p,
                                bestDistance,
                                similarity
                        )
                );
            }
        }

        return results;
    }

    // ============================================================
    // FUZZY FIELD DISTANCE
    // ============================================================

    static int fuzzyFieldDistance(
            String query,
            String field
    ) {

        String text = field.toLowerCase();

        if (text.equals(query)) {
            return 0;
        }

        if (text.contains(query)) {
            return 0;
        }

        String[] words = text.split("\\s+");

        int best =
                levenshteinDistance(
                        query,
                        text
                );

        for (String word : words) {

            int d =
                    levenshteinDistance(
                            query,
                            word
                    );

            best = Math.min(best, d);
        }

        return best;
    }

    // ============================================================
    // DEPARTMENT FILTER
    // ============================================================

    static List<Patient> filterByDepartment(
            String department
    ) {

        List<Patient> result =
                new ArrayList<>();

        for (Patient p : patients) {

            if (p.department.equalsIgnoreCase(
                    department
            )) {
                result.add(p);
            }
        }

        return result;
    }

    // ============================================================
    // STATUS FILTER
    // ============================================================

    static List<Patient> filterByStatus(
            String status
    ) {

        List<Patient> result =
                new ArrayList<>();

        for (Patient p : patients) {

            if (p.status.name().equalsIgnoreCase(
                    status
            )) {
                result.add(p);
            }
        }

        return result;
    }

    // ============================================================
    // LEVENSHTEIN DISTANCE USING DYNAMIC PROGRAMMING
    // ============================================================

    static int levenshteinDistance(String s1, String s2) {

        int[][] dp = buildLevenshteinMatrix(s1, s2);
        return dp[s1.length()][s2.length()];
    }

    // Build the complete Wagner-Fischer / Levenshtein DP matrix.
    static int[][] buildLevenshteinMatrix(String s1, String s2) {

        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException(
                    "Strings cannot be null."
            );
        }

        s1 = s1.toLowerCase(Locale.ROOT);
        s2 = s2.toLowerCase(Locale.ROOT);

        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];

        // Base case: convert a string to an empty string.
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        // Base case: convert an empty string to a string.
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        // Fill the DP matrix.
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {

                int cost =
                        (s1.charAt(i - 1) == s2.charAt(j - 1))
                                ? 0
                                : 1;

                int deletion = dp[i - 1][j] + 1;
                int insertion = dp[i][j - 1] + 1;
                int substitution = dp[i - 1][j - 1] + cost;

                dp[i][j] = Math.min(
                        Math.min(deletion, insertion),
                        substitution
                );
            }
        }

        return dp;
    }

    // ============================================================
    // DYNAMIC PROGRAMMING MATRIX
    // ============================================================

    static void displayDPMatixMenu() {

        System.out.println(
                "\n=========== DYNAMIC PROGRAMMING MATRIX ==========="
        );

        String s1 = readLine("Enter first string: ");
        String s2 = readLine("Enter second string: ");

        displayDPMatrix(s1, s2);
    }

    static void displayDPMatrix(String s1, String s2) {

        if (s1 == null || s2 == null) {
            System.out.println("Strings cannot be null.");
            return;
        }

        String first = s1.toLowerCase(Locale.ROOT);
        String second = s2.toLowerCase(Locale.ROOT);

        int[][] dp = buildLevenshteinMatrix(first, second);

        int m = first.length();
        int n = second.length();

        System.out.println("\nLevenshtein DP Matrix:");

        // Top header: empty string followed by characters of second string.
        // The empty-string header is shifted right to align with dp[0][0].
        System.out.printf("%12s", "\"\"");
        for (int j = 0; j < n; j++) {
            System.out.printf("%5c", second.charAt(j));
        }
        System.out.println();

        // Matrix rows. The row label occupies the left side, and the
        // numerical values start one box to the left of the previous version.
        for (int i = 0; i <= m; i++) {

            if (i == 0) {
                System.out.printf("%-6s", "\"\"");
            } else {
                System.out.printf("%-6c", first.charAt(i - 1));
            }

            for (int j = 0; j <= n; j++) {
                System.out.printf("%5d", dp[i][j]);
            }

            System.out.println();
        }

        System.out.println("\nEdit Distance = " + dp[m][n]);
    }

    // ============================================================
    // MEDICAL CORPUS
    // ============================================================

    static void initializeMedicalCorpus() {

        MEDICAL_CORPUS.clear();

        // ---------------- DIAGNOSIS ----------------

        addMedicalEntry(
                "dx-1",
                "Diabetes Mellitus",
                Category.DIAGNOSIS,
                "Diagnosis / Endocrine",
                "Metabolic disorder characterized by elevated blood glucose.",
                "Increased thirst, frequent urination and fatigue.",
                "Blood glucose and HbA1c are commonly evaluated.",
                "dibetes",
                "diabtes",
                "diabitis"
        );

        addMedicalEntry(
                "dx-2",
                "Bronchial Asthma",
                Category.DIAGNOSIS,
                "Diagnosis / Pulmonology",
                "Chronic inflammatory airway disorder.",
                "Wheezing, coughing and shortness of breath.",
                "Spirometry may be used for assessment.",
                "asthema",
                "astma",
                "bronchial asma"
        );

        addMedicalEntry(
                "dx-3",
                "Atherosclerosis",
                Category.DIAGNOSIS,
                "Diagnosis / Cardiovascular",
                "Narrowing of arteries due to plaque accumulation.",
                "Angina, claudication and vascular symptoms.",
                "Associated with cardiovascular disease.",
                "atheroclerosis",
                "athrosclerosis",
                "athersclerosis"
        );

        addMedicalEntry(
                "dx-4",
                "Pneumonia",
                Category.DIAGNOSIS,
                "Diagnosis / Pulmonology",
                "Infection involving pulmonary tissue.",
                "Fever, cough and breathing difficulty.",
                "Chest imaging may support diagnosis.",
                "pnumonia",
                "newmonia",
                "pneumonea"
        );

        addMedicalEntry(
                "dx-5",
                "Myocardial Infarction",
                Category.DIAGNOSIS,
                "Diagnosis / Cardiology",
                "Acute myocardial tissue injury.",
                "Chest discomfort and shortness of breath.",
                "ECG and laboratory testing are commonly used.",
                "myocadial infarction",
                "myocardial infarcation",
                "myocardial infarctuin"
        );

        addMedicalEntry(
                "dx-6",
                "Gastroenteritis",
                Category.DIAGNOSIS,
                "Diagnosis / Gastroenterology",
                "Inflammation of the stomach and intestines.",
                "Diarrhea, vomiting and abdominal cramps.",
                "Hydration is an important component of management.",
                "gastroentritis",
                "gastroenteritus",
                "gastointestinal"
        );

        addMedicalEntry(
                "dx-7",
                "Hypothyroidism",
                Category.DIAGNOSIS,
                "Diagnosis / Endocrine",
                "Reduced thyroid hormone production.",
                "Fatigue, cold intolerance and constipation.",
                "TSH and thyroid hormone tests may be evaluated.",
                "hypothiroidism",
                "hypothroidism",
                "hipothyroidism"
        );

        addMedicalEntry(
                "dx-8",
                "Multiple Sclerosis",
                Category.DIAGNOSIS,
                "Diagnosis / Neurology",
                "Chronic demyelinating disorder of the central nervous system.",
                "Sensory changes, weakness and visual symptoms.",
                "MRI may be used as part of evaluation.",
                "multiple sclorosis",
                "multipul sclerosis",
                "multiple sceloris"
        );

        addMedicalEntry(
                "dx-9",
                "Osteoarthritis",
                Category.DIAGNOSIS,
                "Diagnosis / Rheumatology",
                "Degenerative joint disease.",
                "Joint pain and stiffness.",
                "Imaging may demonstrate degenerative changes.",
                "osteoarthrits",
                "ostioarthritis",
                "ostearthritis"
        );

        addMedicalEntry(
                "dx-10",
                "Tuberculosis",
                Category.DIAGNOSIS,
                "Diagnosis / Infectious Disease",
                "Bacterial infectious disease.",
                "Persistent cough and systemic symptoms.",
                "Laboratory and imaging tests may be used.",
                "tuberculocis",
                "tuberculoses",
                "tuberclosis"
        );

        // ---------------- DRUGS ----------------

        addMedicalEntry(
                "drug-1",
                "Paracetamol",
                Category.DRUG,
                "Medication / Analgesic",
                "Common analgesic and antipyretic medicine.",
                "Pain and fever.",
                "Dosage should be determined appropriately.",
                "paracetmol",
                "paracatmol",
                "paracetamol"
        );

        addMedicalEntry(
                "drug-2",
                "Amoxicillin",
                Category.DRUG,
                "Medication / Antibiotic",
                "Beta-lactam antibiotic.",
                "Certain bacterial infections.",
                "Use depends on clinical indication.",
                "amoxiclin",
                "amoxcillin",
                "amoxycillin"
        );

        addMedicalEntry(
                "drug-3",
                "Ibuprofen",
                Category.DRUG,
                "Medication / NSAID",
                "Non-steroidal anti-inflammatory medicine.",
                "Pain and inflammation.",
                "Use should follow appropriate medical guidance.",
                "ibuprofn",
                "ibuprofren"
        );

        addMedicalEntry(
                "drug-4",
                "Cephalexin",
                Category.DRUG,
                "Medication / Antibiotic",
                "Cephalosporin antibiotic.",
                "Certain bacterial infections.",
                "Use depends on clinical indication.",
                "cephlaxin",
                "cefalexin"
        );

        addMedicalEntry(
                "drug-5",
                "Atorvastatin",
                Category.DRUG,
                "Medication / Lipid lowering",
                "Statin medicine used to reduce cholesterol.",
                "Lipid management.",
                "Monitoring depends on clinical context.",
                "atorvastin",
                "atorvastatin"
        );

        addMedicalEntry(
                "drug-6",
                "Azithromycin",
                Category.DRUG,
                "Medication / Antibiotic",
                "Macrolide antibiotic.",
                "Certain bacterial infections.",
                "Use depends on clinical indication.",
                "azithromicin",
                "azithromicyn"
        );

        addMedicalEntry(
                "drug-7",
                "Cetirizine",
                Category.DRUG,
                "Medication / Antihistamine",
                "Antihistamine medication.",
                "Allergic symptoms.",
                "Use according to appropriate instructions.",
                "cetrizine",
                "cetrazine"
        );

        addMedicalEntry(
                "drug-8",
                "Losartan",
                Category.DRUG,
                "Medication / Cardiovascular",
                "Angiotensin receptor blocker.",
                "Blood pressure management.",
                "Clinical monitoring may be required.",
                "losartin",
                "losartan"
        );

        // ---------------- SYMPTOMS ----------------

        addMedicalEntry(
                "sym-1",
                "Hypertension",
                Category.SYMPTOM,
                "Symptom / Cardiovascular",
                "Elevated blood pressure measurement.",
                "May occur without obvious symptoms.",
                "Blood pressure measurement is required.",
                "hypertnnsion",
                "hypertention"
        );

        addMedicalEntry(
                "sym-2",
                "Arrhythmia",
                Category.SYMPTOM,
                "Symptom / Cardiology",
                "Abnormal heart rhythm.",
                "Palpitations and irregular heartbeat.",
                "ECG can assist assessment.",
                "arrythmia",
                "arythmia"
        );

        addMedicalEntry(
                "sym-3",
                "Dyspnea",
                Category.SYMPTOM,
                "Symptom / Respiratory",
                "Shortness of breath.",
                "Breathing difficulty.",
                "Clinical assessment determines the cause.",
                "dispnea",
                "dyspnea"
        );

        addMedicalEntry(
                "sym-4",
                "Nausea",
                Category.SYMPTOM,
                "Symptom / Gastrointestinal",
                "Feeling of needing to vomit.",
                "May occur with gastrointestinal conditions.",
                "Cause should be evaluated clinically.",
                "nausia",
                "nause"
        );

        addMedicalEntry(
                "sym-5",
                "Fatigue",
                Category.SYMPTOM,
                "Symptom / General",
                "Feeling of tiredness or low energy.",
                "Occurs across many conditions.",
                "Clinical context is important.",
                "fatige",
                "fatique"
        );
    }

    static void addMedicalEntry(
            String id,
            String term,
            Category category,
            String label,
            String definition,
            String indications,
            String clinicalNotes,
            String... misspellings
    ) {

        MEDICAL_CORPUS.add(
                new MedicalEntry(
                        id,
                        term,
                        category,
                        label,
                        definition,
                        indications,
                        clinicalNotes,
                        misspellings
                )
        );
    }

    // ============================================================
    // MEDICAL DICTIONARY MENU
    // ============================================================

    static void medicalDictionary() {

        while (true) {

            System.out.println(
                    "\n================ MEDICAL CORPUS ================"
            );

            System.out.println("1. Display all terms");
            System.out.println("2. Search dictionary");
            System.out.println("3. View entry details");
            System.out.println("4. Back");

            int choice =
                    readInt("Choice: ");

            switch (choice) {

                case 1:

                    for (MedicalEntry e :
                            MEDICAL_CORPUS) {

                        System.out.println(
                                e.term +
                                " [" +
                                e.category +
                                "]"
                        );
                    }

                    break;

                case 2:

                    String query =
                            readLine("Search term: ");

                    for (MedicalEntry e :
                            MEDICAL_CORPUS) {

                        int d =
                                fuzzyFieldDistance(
                                        query,
                                        e.term
                                );

                        if (d <= 3 ||
                                e.term
                                        .toLowerCase()
                                        .contains(
                                                query.toLowerCase()
                                        )) {

                            System.out.println(
                                    e.term +
                                    " | Distance: " +
                                    d
                            );
                        }
                    }

                    break;

                case 3:

                    String term =
                            readLine("Enter term: ");

                    MedicalEntry entry =
                            findMedicalEntry(term);

                    if (entry != null) {

                        displayMedicalEntry(entry);

                    } else {

                        System.out.println(
                                "Entry not found."
                        );
                    }

                    break;

                case 4:
                    return;

                default:
                    System.out.println(
                            "Invalid choice."
                    );
            }
        }
    }

    // ============================================================
    // FIND MEDICAL ENTRY
    // ============================================================

    static MedicalEntry findMedicalEntry(
            String term
    ) {

        for (MedicalEntry e :
                MEDICAL_CORPUS) {

            if (e.term.equalsIgnoreCase(term)) {
                return e;
            }
        }

        return null;
    }

    // ============================================================
    // DISPLAY MEDICAL ENTRY
    // ============================================================

    static void displayMedicalEntry(
            MedicalEntry e
    ) {

        System.out.println(
                "\n================================================"
        );

        System.out.println(
                "Term          : " + e.term
        );

        System.out.println(
                "Category      : " + e.category
        );

        System.out.println(
                "Category Label: " + e.categoryLabel
        );

        System.out.println(
                "Definition    : " + e.definition
        );

        System.out.println(
                "Indications   : " + e.indications
        );

        System.out.println(
                "Clinical Notes: " + e.clinicalNotes
        );

        System.out.println(
                "Common Typos  : " +
                String.join(
                        ", ",
                        e.commonMisspellings
                )
        );

        System.out.println(
                "================================================"
        );
    }

    // ============================================================
    // SPELLING CORRECTION MENU
    // ============================================================

    static void spellingCorrectionMenu() {

        System.out.println(
                "\n=============== SPELLING CORRECTION ==============="
        );

        String query =
                readLine("Enter medical term: ");

        SpellingMistakeDetection result =
                detectAndCorrectMedicalMistake(
                        query,
                        null
                );

        if (result == null) {

            System.out.println(
                    "No suitable dictionary match found."
            );

            return;
        }

        System.out.println(result);

        if (result.editDistance > 0) {

            System.out.println(
                    "\nAUTO-CORRECTION:"
            );

            System.out.println(
                    query +
                    " -> " +
                    result.correctedTerm
            );
        }
    }

    // ============================================================
    // SPELLING DETECTION
    // ============================================================

    static SpellingMistakeDetection
    detectAndCorrectMedicalMistake(
            String rawQuery,
            Category targetCategory
    ) {

        if (rawQuery == null) {
            return null;
        }

        String query =
                rawQuery.trim().toLowerCase();

        if (query.length() < 2) {
            return null;
        }

        MedicalEntry bestMatch = null;

        int bestDistance =
                Integer.MAX_VALUE;

        String bestMatchedWord = "";

        boolean knownMisspelling = false;

        for (MedicalEntry entry :
                MEDICAL_CORPUS) {

            if (targetCategory != null &&
                    entry.category != targetCategory) {
                continue;
            }

            // Known typo
            for (String typo :
                    entry.commonMisspellings) {

                if (typo.equalsIgnoreCase(query)) {

                    bestMatch = entry;
                    bestDistance = 1;
                    bestMatchedWord = entry.term;
                    knownMisspelling = true;

                    break;
                }
            }

            if (knownMisspelling) {
                break;
            }

            // Full term
            int fullDistance =
                    levenshteinDistance(
                            query,
                            entry.term.toLowerCase()
                    );

            if (fullDistance <
                    bestDistance) {

                bestDistance = fullDistance;
                bestMatch = entry;
                bestMatchedWord = entry.term;
            }

            // Individual words
            String[] words =
                    entry.term
                            .toLowerCase()
                            .split("\\s+");

            for (String word : words) {

                int d =
                        levenshteinDistance(
                                query,
                                word
                        );

                if (d < bestDistance) {

                    bestDistance = d;
                    bestMatch = entry;
                    bestMatchedWord = word;
                }
            }
        }

        if (bestMatch == null) {
            return null;
        }

        String target =
                bestMatchedWord.isEmpty()
                        ? bestMatch.term.toLowerCase()
                        : bestMatchedWord.toLowerCase();

        int maxLength =
                Math.max(
                        query.length(),
                        target.length()
                );

        int similarity =
                Math.max(
                        10,
                        (int) Math.round(
                                (
                                        1.0 -
                                        (
                                                (double) bestDistance /
                                                maxLength
                                        )
                                ) * 100
                        )
                );

        SpellingMistakeDetection result =
                new SpellingMistakeDetection();

        result.originalQuery = rawQuery;
        result.correctedTerm = bestMatch.term;
        result.category = bestMatch.category;
        result.categoryLabel =
                bestMatch.categoryLabel;
        result.editDistance = bestDistance;
        result.similarity = similarity;
        result.matchedEntry = bestMatch;
        result.matchedWord = bestMatchedWord;

        // Exact match
        if (
                query.equals(target) ||
                query.equals(
                        bestMatch.term.toLowerCase()
                )
        ) {

            result.errorType =
                    ErrorType.EXACT_MATCH;

            result.editDistance = 0;
            result.similarity = 100;

            result.explanation =
                    "'" +
                    rawQuery +
                    "' is an exact match for '" +
                    bestMatch.term +
                    "'.";

            return result;
        }

        int allowedDistance;

        if (query.length() <= 4) {
            allowedDistance = 1;
        } else if (query.length() <= 7) {
            allowedDistance = 2;
        } else {
            allowedDistance = 3;
        }

        if (
                bestDistance > allowedDistance &&
                !knownMisspelling
        ) {
            return null;
        }

        int lengthDifference =
                query.length() -
                target.length();

        // Omission
        if (lengthDifference < 0) {

            result.errorType =
                    ErrorType.OMISSION;

            int missing =
                    Math.abs(lengthDifference);

            result.explanation =
                    "Omission error: " +
                    missing +
                    " character(s) may have been omitted.";

        }

        // Insertion
        else if (lengthDifference > 0) {

            result.errorType =
                    ErrorType.INSERTION;

            result.explanation =
                    "Insertion error: " +
                    lengthDifference +
                    " extra character(s) may have been entered.";

        }

        // Same length
        else {

            int differences = 0;

            for (int i = 0;
                 i < query.length();
                 i++) {

                if (
                        query.charAt(i) !=
                        target.charAt(i)
                ) {
                    differences++;
                }
            }

            if (differences == 2) {

                result.errorType =
                        ErrorType.TRANSPOSITION;

                result.explanation =
                        "Transposition error: characters may have been swapped.";

            } else {

                result.errorType =
                        ErrorType.SUBSTITUTION;

                result.explanation =
                        "Substitution error: one or more characters differ.";
            }
        }

        return result;
    }

    // ============================================================
    // AUTO CORRECT DIAGNOSIS
    // ============================================================

    static String autoCorrectDiagnosis(
            String input
    ) {

        SpellingMistakeDetection result =
                detectAndCorrectMedicalMistake(
                        input,
                        Category.DIAGNOSIS
                );

        if (
                result != null &&
                result.editDistance > 0
        ) {
            return result.correctedTerm;
        }

        return input;
    }

    // ============================================================
    // AUTO CORRECT MEDICATION
    // ============================================================

    static String autoCorrectMedication(
            String input
    ) {

        SpellingMistakeDetection result =
                detectAndCorrectMedicalMistake(
                        input,
                        Category.DRUG
                );

        if (
                result != null &&
                result.editDistance > 0
        ) {
            return result.correctedTerm;
        }

        return input;
    }

    // ============================================================
    // AUTO CORRECT SYMPTOMS
    // ============================================================

    static String autoCorrectSymptoms(
            String input
    ) {

        String[] parts =
                input.split(",");

        List<String> corrected =
                new ArrayList<>();

        for (String part : parts) {

            String value =
                    part.trim();

            SpellingMistakeDetection result =
                    detectAndCorrectMedicalMistake(
                            value,
                            Category.SYMPTOM
                    );

            if (
                    result != null &&
                    result.editDistance > 0
            ) {

                corrected.add(
                        result.correctedTerm
                );

            } else {

                corrected.add(value);
            }
        }

        return String.join(
                ", ",
                corrected
        );
    }

    // ============================================================
    // EXACT VS FUZZY COMPARISON
    // ============================================================

    static void exactVsFuzzyComparison() {

        System.out.println(
                "\n=========== EXACT VS FUZZY SEARCH ==========="
        );

        String query =
                readLine("Enter query: ");

        System.out.println(
                "\nQuery: " + query
        );

        System.out.println(
                "\nEXACT SEARCH:"
        );

        boolean foundExact = false;

        for (MedicalEntry e :
                MEDICAL_CORPUS) {

            if (
                    e.term.equalsIgnoreCase(query)
            ) {

                System.out.println(
                        "MATCH -> " +
                        e.term
                );

                foundExact = true;
            }
        }

        if (!foundExact) {

            System.out.println(
                    "No exact match."
            );
        }

        System.out.println(
                "\nFUZZY SEARCH:"
        );

        for (MedicalEntry e :
                MEDICAL_CORPUS) {

            int d =
                    fuzzyFieldDistance(
                            query,
                            e.term
                    );

            if (d <= 3) {

                System.out.println(
                        e.term +
                        " | Distance = " +
                        d
                );
            }
        }
    }

    // ============================================================
    // BENCHMARK
    // ============================================================

    static void benchmarkSystem() {

        System.out.println(
                "\n================ BENCHMARK ================="
        );

        List<BenchmarkTestCase> tests =
                Arrays.asList(

                        new BenchmarkTestCase(
                                "paracetmol",
                                "Paracetamol",
                                Category.DRUG,
                                ErrorType.OMISSION,
                                1
                        ),

                        new BenchmarkTestCase(
                                "dibetes",
                                "Diabetes Mellitus",
                                Category.DIAGNOSIS,
                                ErrorType.OMISSION,
                                2
                        ),

                        new BenchmarkTestCase(
                                "amoxiclin",
                                "Amoxicillin",
                                Category.DRUG,
                                ErrorType.OMISSION,
                                2
                        ),

                        new BenchmarkTestCase(
                                "hypertnnsion",
                                "Hypertension",
                                Category.SYMPTOM,
                                ErrorType.SUBSTITUTION,
                                1
                        ),

                        new BenchmarkTestCase(
                                "asthema",
                                "Bronchial Asthma",
                                Category.DIAGNOSIS,
                                ErrorType.INSERTION,
                                1
                        ),

                        new BenchmarkTestCase(
                                "ibuprofn",
                                "Ibuprofen",
                                Category.DRUG,
                                ErrorType.OMISSION,
                                1
                        ),

                        new BenchmarkTestCase(
                                "cephlaxin",
                                "Cephalexin",
                                Category.DRUG,
                                ErrorType.OMISSION,
                                2
                        ),

                        new BenchmarkTestCase(
                                "arrythmia",
                                "Arrhythmia",
                                Category.SYMPTOM,
                                ErrorType.SUBSTITUTION,
                                1
                        ),

                        new BenchmarkTestCase(
                                "dispnea",
                                "Dyspnea",
                                Category.SYMPTOM,
                                ErrorType.SUBSTITUTION,
                                2
                        ),

                        new BenchmarkTestCase(
                                "atorvastin",
                                "Atorvastatin",
                                Category.DRUG,
                                ErrorType.OMISSION,
                                1
                        ),

                        new BenchmarkTestCase(
                                "pnumonia",
                                "Pneumonia",
                                Category.DIAGNOSIS,
                                ErrorType.OMISSION,
                                1
                        ),

                        new BenchmarkTestCase(
                                "nausia",
                                "Nausea",
                                Category.SYMPTOM,
                                ErrorType.SUBSTITUTION,
                                1
                        ),

                        new BenchmarkTestCase(
                                "azithromicin",
                                "Azithromycin",
                                Category.DRUG,
                                ErrorType.SUBSTITUTION,
                                1
                        ),

                        new BenchmarkTestCase(
                                "cetrizine",
                                "Cetirizine",
                                Category.DRUG,
                                ErrorType.TRANSPOSITION,
                                1
                        ),

                        new BenchmarkTestCase(
                                "losartin",
                                "Losartan",
                                Category.DRUG,
                                ErrorType.SUBSTITUTION,
                                1
                        )
                );

        int passed = 0;

        long start =
                System.nanoTime();

        for (BenchmarkTestCase test :
                tests) {

            SpellingMistakeDetection result =
                    detectAndCorrectMedicalMistake(
                            test.query,
                            test.category
                    );

            boolean correct =
                    result != null &&
                    result.correctedTerm
                            .equalsIgnoreCase(
                                    test.target
                            );

            if (correct) {
                passed++;
            }

            System.out.println(
                    (correct ? "PASS" : "FAIL") +
                    " | Query: " +
                    test.query +
                    " | Expected: " +
                    test.target +
                    (
                            result == null
                                    ? ""
                                    : " | Found: " +
                                      result.correctedTerm
                    )
            );
        }

        long end =
                System.nanoTime();

        double elapsed =
                (end - start) /
                1_000_000.0;

        double accuracy =
                passed * 100.0 /
                tests.size();

        System.out.println(
                "\n--------------------------------------------"
        );

        System.out.println(
                "Total Tests : " +
                tests.size()
        );

        System.out.println(
                "Passed      : " +
                passed
        );

        System.out.println(
                "Failed      : " +
                (tests.size() - passed)
        );

        System.out.printf(
                "Accuracy    : %.2f%%%n",
                accuracy
        );

        System.out.printf(
                "Time        : %.3f ms%n",
                elapsed
        );

        System.out.println(
                "--------------------------------------------"
        );
    }

    // ============================================================
    // STATISTICS
    // ============================================================

    static void showStatistics() {

        int inpatient = 0;
        int outpatient = 0;
        int discharged = 0;
        int critical = 0;

        Map<String, Integer> departments =
                new TreeMap<>();

        for (Patient p :
                patients) {

            switch (p.status) {

                case INPATIENT:
                    inpatient++;
                    break;

                case OUTPATIENT:
                    outpatient++;
                    break;

                case DISCHARGED:
                    discharged++;
                    break;

                case CRITICAL:
                    critical++;
                    break;
            }

            departments.put(
                    p.department,
                    departments.getOrDefault(
                            p.department,
                            0
                    ) + 1
            );
        }

        System.out.println(
                "\n================ STATISTICS ================"
        );

        System.out.println(
                "Total Patients : " +
                patients.size()
        );

        System.out.println(
                "Inpatients     : " +
                inpatient
        );

        System.out.println(
                "Outpatients    : " +
                outpatient
        );

        System.out.println(
                "Discharged     : " +
                discharged
        );

        System.out.println(
                "Critical       : " +
                critical
        );

        System.out.println(
                "\nDepartment Distribution:"
        );

        for (Map.Entry<String, Integer> e :
                departments.entrySet()) {

            System.out.printf(
                    "%-25s : %d%n",
                    e.getKey(),
                    e.getValue()
            );
        }
    }

    // ============================================================
    // ADD PATIENT
    // ============================================================

    static void addPatient() {

        System.out.println(
                "\n================ ADD PATIENT ================"
        );

        String id =
                generateNextPatientId();

        String name =
                readLine("Patient Name: ");

        int age =
                readInt("Age: ");

        String genderInput =
                readLine(
                        "Gender (MALE/FEMALE/OTHER): "
                );

        Gender gender;

        try {

            gender =
                    Gender.valueOf(
                            genderInput.toUpperCase()
                    );

        } catch (Exception e) {

            gender = Gender.OTHER;
        }

        String bloodGroup =
                readLine("Blood Group: ");

        String contact =
                readLine("Contact: ");

        String diagnosisInput =
                readLine("Diagnosis: ");

        String diagnosis =
                autoCorrectDiagnosis(
                        diagnosisInput
                );

        String department =
                readLine("Department: ");

        String symptomInput =
                readLine(
                        "Symptoms separated by comma: "
                );

        String correctedSymptoms =
                autoCorrectSymptoms(
                        symptomInput
                );

        List<String> symptoms =
                new ArrayList<>();

        for (String s :
                correctedSymptoms.split(",")) {

            if (!s.trim().isEmpty()) {
                symptoms.add(s.trim());
            }
        }

        List<PatientMedication> medications =
                new ArrayList<>();

        String addMore;

        do {

            String drugInput =
                    readLine(
                            "Medication name: "
                    );

            String drug =
                    autoCorrectMedication(
                            drugInput
                    );

            String dosage =
                    readLine("Dosage: ");

            String frequency =
                    readLine("Frequency: ");

            medications.add(
                    new PatientMedication(
                            drug,
                            dosage,
                            frequency
                    )
            );

            addMore =
                    readLine(
                            "Add another medication? (Y/N): "
                    );

        } while (
                addMore.equalsIgnoreCase("Y")
        );

        String physician =
                readLine(
                        "Attending Physician: "
                );

        String statusInput =
                readLine(
                        "Status (INPATIENT/OUTPATIENT/DISCHARGED/CRITICAL): "
                );

        Status status;

        try {

            status =
                    Status.valueOf(
                            statusInput.toUpperCase()
                    );

        } catch (Exception e) {

            status = Status.OUTPATIENT;
        }

        String admissionDate =
                LocalDate.now().toString();

        String notes =
                "Patient record created on " +
                admissionDate +
                ". Diagnosis: " +
                diagnosis +
                ". Department: " +
                department +
                ". Status: " +
                status +
                ".";

        Patient p =
                new Patient(
                        id,
                        name,
                        age,
                        gender,
                        bloodGroup,
                        contact,
                        admissionDate,
                        diagnosis,
                        symptoms,
                        medications,
                        physician,
                        department,
                        status,
                        notes
                );

        patients.add(0, p);

        System.out.println(
                "\nPatient successfully added."
        );

        System.out.println(
                "Generated Patient ID: " +
                id
        );
    }

    // ============================================================
    // GENERATE NEXT PATIENT ID
    // ============================================================

    static String generateNextPatientId() {

        int max = 1000;

        for (Patient p :
                patients) {

            try {

                String number =
                        p.id.replace(
                                "PID-",
                                ""
                        );

                int value =
                        Integer.parseInt(number);

                max =
                        Math.max(
                                max,
                                value
                        );

            } catch (Exception ignored) {
            }
        }

        return "PID-" + (max + 1);
    }

    // ============================================================
    // RESET PATIENTS
    // ============================================================

    static void resetPatients() {

        System.out.println(
                "\nResetting patient database..."
        );

        patients =
                generate1000Patients();

        System.out.println(
                "Database reset to " +
                patients.size() +
                " patients."
        );
    }

    // ============================================================
    // FIND PATIENT BY ID
    // ============================================================

    static Patient findPatientById(
            String id
    ) {

        for (Patient p :
                patients) {

            if (p.id.equalsIgnoreCase(id)) {
                return p;
            }
        }

        return null;
    }

    // ============================================================
    // DETERMINISTIC RANDOM GENERATOR
    // ============================================================

    static class SeededRandom {

        long seed;

        SeededRandom(long seed) {

            seed %= 2147483647;

            if (seed <= 0) {
                seed += 2147483646;
            }

            this.seed = seed;
        }

        double nextDouble() {

            seed =
                    (seed * 16807) %
                    2147483647;

            return
                    (double) (seed - 1) /
                    2147483646;
        }

        int nextInt(int bound) {

            return
                    (int)
                    (nextDouble() * bound);
        }
    }

    // ============================================================
    // GENERATE 1,000 PATIENTS
    // ============================================================

    static List<Patient>
    generate1000Patients() {

        List<Patient> result =
                new ArrayList<>();

        Set<String> usedNames =
                new HashSet<>();

        Set<String> usedPhones =
                new HashSet<>();

        DiagnosisTemplate[] templates =
                getDiagnosisTemplates();

        for (int i = 1;
             i <= 1000;
             i++) {

            SeededRandom rng =
                    new SeededRandom(
                            i * 7919L + 104729
                    );

            // Unique name
            String name;

            do {

                String first =
                        FIRST_NAMES[
                                rng.nextInt(
                                        FIRST_NAMES.length
                                )
                        ];

                String last =
                        LAST_NAMES[
                                rng.nextInt(
                                        LAST_NAMES.length
                                )
                        ];

                String middle =
                        MIDDLE_INITIALS[
                                rng.nextInt(
                                        MIDDLE_INITIALS.length
                                )
                        ];

                name =
                        first +
                        " " +
                        middle +
                        " " +
                        last;

            } while (
                    usedNames.contains(name)
            );

            usedNames.add(name);

            // Unique phone
            String contact;

            do {

                String[] prefixes = {
                        "98480", "94401",
                        "91772", "89781",
                        "79890", "96180",
                        "99890", "88970",
                        "90001", "95020"
                };

                String prefix =
                        prefixes[
                                rng.nextInt(
                                        prefixes.length
                                )
                        ];

                int number =
                        10000 +
                        rng.nextInt(90000);

                contact =
                        "+91 " +
                        prefix +
                        " " +
                        number;

            } while (
                    usedPhones.contains(contact)
            );

            usedPhones.add(contact);

            // Demographics
            int age =
                    18 +
                    rng.nextInt(72);

            double genderRoll =
                    rng.nextDouble();

            Gender gender;

            if (genderRoll < 0.49) {
                gender = Gender.MALE;
            } else if (genderRoll < 0.98) {
                gender = Gender.FEMALE;
            } else {
                gender = Gender.OTHER;
            }

            String bloodGroup =
                    BLOOD_GROUPS[
                            rng.nextInt(
                                    BLOOD_GROUPS.length
                            )
                    ];

            // Admission date
            LocalDate date =
                    LocalDate.of(
                            2026,
                            9,
                            18
                    ).minusDays(
                            rng.nextInt(365)
                    );

            String admissionDate =
                    date.toString();

            // Diagnosis
            DiagnosisTemplate template =
                    templates[
                            i % templates.length
                    ];

            String diagnosis =
                    template.subtypes[
                            rng.nextInt(
                                    template.subtypes.length
                            )
                    ];

            // Symptoms
            List<String> symptoms =
                    new ArrayList<>(
                            Arrays.asList(
                                    template.symptoms
                            )
                    );

            Collections.shuffle(
                    symptoms,
                    new Random(
                            i * 1009L
                    )
            );

            int symptomCount =
                    2 +
                    rng.nextInt(
                            Math.min(
                                    4,
                                    symptoms.size() - 1
                            )
                    );

            symptoms =
                    new ArrayList<>(
                            symptoms.subList(
                                    0,
                                    symptomCount
                            )
                    );

            // Medications
            List<MedicationTemplate> medPool =
                    Arrays.asList(
                            template.medications
                    );

            List<MedicationTemplate> shuffledMeds =
                    new ArrayList<>(
                            medPool
                    );

            Collections.shuffle(
                    shuffledMeds,
                    new Random(
                            i * 2027L
                    )
            );

            int medicationCount =
                    1 +
                    rng.nextInt(
                            Math.min(
                                    3,
                                    shuffledMeds.size()
                            )
                    );

            List<PatientMedication> medications =
                    new ArrayList<>();

            for (int m = 0;
                 m < medicationCount;
                 m++) {

                MedicationTemplate med =
                        shuffledMeds.get(m);

                String dosage =
                        med.dosages[
                                rng.nextInt(
                                        med.dosages.length
                                )
                        ];

                String frequency =
                        med.frequencies[
                                rng.nextInt(
                                        med.frequencies.length
                                )
                        ];

                medications.add(
                        new PatientMedication(
                                med.drugName,
                                dosage,
                                frequency
                        )
                );
            }

            // Physician
            String physician =
                    PHYSICIANS[
                            rng.nextInt(
                                    PHYSICIANS.length
                            )
                    ];

            // Status
            double statusRoll =
                    rng.nextDouble();

            Status status;

            if (statusRoll < 0.20) {
                status = Status.INPATIENT;
            } else if (statusRoll < 0.28) {
                status = Status.CRITICAL;
            } else if (statusRoll < 0.75) {
                status = Status.OUTPATIENT;
            } else {
                status = Status.DISCHARGED;
            }

            // Vitals
            int systolic =
                    110 +
                    rng.nextInt(45);

            int diastolic =
                    70 +
                    rng.nextInt(25);

            int heartRate =
                    62 +
                    rng.nextInt(38);

            int spo2 =
                    94 +
                    rng.nextInt(6);

            double temperature =
                    97.8 +
                    rng.nextDouble() * 2.6;

            int bloodSugar =
                    85 +
                    rng.nextInt(140);

            int painScore =
                    rng.nextInt(8);

            StringBuilder medicationSummary =
                    new StringBuilder();

            for (PatientMedication med :
                    medications) {

                if (medicationSummary.length() > 0) {
                    medicationSummary.append(", ");
                }

                medicationSummary
                        .append(med.drugName)
                        .append(" ")
                        .append(med.dosage);
            }

            String notes =
                    "Clinical Evaluation on " +
                    admissionDate +
                    ": Patient presents with " +
                    String.join(
                            ", ",
                            symptoms
                    ).toLowerCase() +
                    ". Vitals: BP " +
                    systolic +
                    "/" +
                    diastolic +
                    " mmHg, HR " +
                    heartRate +
                    " bpm, SpO2 " +
                    spo2 +
                    "%, Temp " +
                    String.format(
                            Locale.US,
                            "%.1f",
                            temperature
                    ) +
                    " F, Random Blood Sugar " +
                    bloodSugar +
                    " mg/dL, Pain Score " +
                    painScore +
                    "/10. Medication regimen: " +
                    medicationSummary +
                    ". Status: " +
                    status +
                    ". Physician: " +
                    physician +
                    ". Department: " +
                    template.department +
                    ".";

            Patient patient =
                    new Patient(
                            "PID-" +
                            (1000 + i),
                            name,
                            age,
                            gender,
                            bloodGroup,
                            contact,
                            admissionDate,
                            diagnosis,
                            symptoms,
                            medications,
                            physician,
                            template.department,
                            status,
                            notes
                    );

            result.add(patient);
        }

        return result;
    }

    // ============================================================
    // DIAGNOSIS TEMPLATES
    // ============================================================

    static DiagnosisTemplate[]
    getDiagnosisTemplates() {

        return new DiagnosisTemplate[] {

                new DiagnosisTemplate(
                        "Diabetes Mellitus",
                        "Endocrinology",

                        new String[] {
                                "Type 2 Diabetes Mellitus",
                                "Type 1 Diabetes Mellitus",
                                "Diabetes with Peripheral Neuropathy",
                                "Diabetes with Hyperglycemia"
                        },

                        new String[] {
                                "Fatigue",
                                "Polyuria",
                                "Polydipsia",
                                "Blurred Vision",
                                "Numbness",
                                "Increased Thirst"
                        },

                        new MedicationTemplate[] {

                                new MedicationTemplate(
                                        "Metformin",
                                        new String[] {
                                                "500mg",
                                                "850mg",
                                                "1000mg"
                                        },
                                        new String[] {
                                                "Once daily",
                                                "Twice daily with meals"
                                        }
                                ),

                                new MedicationTemplate(
                                        "Glimepiride",
                                        new String[] {
                                                "1mg",
                                                "2mg",
                                                "4mg"
                                        },
                                        new String[] {
                                                "Once daily before breakfast"
                                        }
                                )
                        }
                ),

                new DiagnosisTemplate(
                        "Pneumonia",
                        "Pulmonology",

                        new String[] {
                                "Community-Acquired Pneumonia",
                                "Bilateral Pneumonia",
                                "Atypical Pneumonia",
                                "Post-Viral Pneumonia"
                        },

                        new String[] {
                                "Dyspnea",
                                "Productive Sputum",
                                "Fever",
                                "Fatigue",
                                "Chest Pain",
                                "Tachycardia",
                                "Chills"
                        },

                        new MedicationTemplate[] {

                                new MedicationTemplate(
                                        "Amoxicillin",
                                        new String[] {
                                                "500mg",
                                                "875mg"
                                        },
                                        new String[] {
                                                "Twice daily",
                                                "Three times daily"
                                        }
                                ),

                                new MedicationTemplate(
                                        "Azithromycin",
                                        new String[] {
                                                "250mg",
                                                "500mg"
                                        },
                                        new String[] {
                                                "Once daily"
                                        }
                                ),

                                new MedicationTemplate(
                                        "Paracetamol",
                                        new String[] {
                                                "500mg",
                                                "650mg"
                                        },
                                        new String[] {
                                                "As needed"
                                        }
                                )
                        }
                ),

                new DiagnosisTemplate(
                        "Osteoarthritis",
                        "Orthopedics",

                        new String[] {
                                "Knee Osteoarthritis",
                                "Hip Osteoarthritis",
                                "Cervical Spondylosis",
                                "Lumbar Spondylosis",
                                "Hand Osteoarthritis"
                        },

                        new String[] {
                                "Arthralgia",
                                "Joint Stiffness",
                                "Morning Stiffness",
                                "Crepitus",
                                "Reduced Mobility",
                                "Myalgia",
                                "Swelling"
                        },

                        new MedicationTemplate[] {

                                new MedicationTemplate(
                                        "Ibuprofen",
                                        new String[] {
                                                "400mg",
                                                "600mg"
                                        },
                                        new String[] {
                                                "Twice daily",
                                                "Three times daily"
                                        }
                                ),

                                new MedicationTemplate(
                                        "Paracetamol",
                                        new String[] {
                                                "650mg",
                                                "1000mg"
                                        },
                                        new String[] {
                                                "As needed"
                                        }
                                )
                        }
                ),

                new DiagnosisTemplate(
                        "Hypothyroidism",
                        "Endocrinology",

                        new String[] {
                                "Primary Hypothyroidism",
                                "Subclinical Hypothyroidism",
                                "Autoimmune Hypothyroidism",
                                "Post-Thyroidectomy Hypothyroidism"
                        },

                        new String[] {
                                "Fatigue",
                                "Cold Intolerance",
                                "Weight Gain",
                                "Dry Skin",
                                "Constipation",
                                "Bradycardia",
                                "Myalgia"
                        },

                        new MedicationTemplate[] {

                                new MedicationTemplate(
                                        "Levothyroxine",
                                        new String[] {
                                                "25mcg",
                                                "50mcg",
                                                "75mcg",
                                                "100mcg"
                                        },
                                        new String[] {
                                                "Once daily before breakfast"
                                        }
                                )
                        }
                ),

                new DiagnosisTemplate(
                        "Gastroenteritis",
                        "Gastroenterology",

                        new String[] {
                                "Acute Viral Gastroenteritis",
                                "Bacterial Gastroenteritis",
                                "Foodborne Enterocolitis",
                                "Acute Enteritis"
                        },

                        new String[] {
                                "Nausea",
                                "Vomiting",
                                "Abdominal Cramps",
                                "Watery Diarrhea",
                                "Dehydration",
                                "Fever",
                                "Fatigue"
                        },

                        new MedicationTemplate[] {

                                new MedicationTemplate(
                                        "Omeprazole",
                                        new String[] {
                                                "20mg",
                                                "40mg"
                                        },
                                        new String[] {
                                                "Once daily"
                                        }
                                ),

                                new MedicationTemplate(
                                        "Ondansetron",
                                        new String[] {
                                                "4mg",
                                                "8mg"
                                        },
                                        new String[] {
                                                "As needed"
                                        }
                                )
                        }
                ),

                new DiagnosisTemplate(
                        "Atherosclerosis",
                        "Cardiology",

                        new String[] {
                                "Coronary Artery Disease",
                                "Peripheral Arterial Disease",
                                "Carotid Atherosclerosis"
                        },

                        new String[] {
                                "Dyspnea",
                                "Arrhythmia",
                                "Chest Discomfort",
                                "Fatigue",
                                "Claudication",
                                "Palpitations"
                        },

                        new MedicationTemplate[] {

                                new MedicationTemplate(
                                        "Atorvastatin",
                                        new String[] {
                                                "20mg",
                                                "40mg",
                                                "80mg"
                                        },
                                        new String[] {
                                                "Once daily"
                                        }
                                ),

                                new MedicationTemplate(
                                        "Aspirin",
                                        new String[] {
                                                "75mg",
                                                "100mg"
                                        },
                                        new String[] {
                                                "Once daily"
                                        }
                                )
                        }
                ),

                new DiagnosisTemplate(
                        "Multiple Sclerosis",
                        "Neurology",

                        new String[] {
                                "Relapsing-Remitting Multiple Sclerosis",
                                "Clinically Isolated Syndrome",
                                "Secondary Progressive MS"
                        },

                        new String[] {
                                "Paresthesia",
                                "Fatigue",
                                "Visual Blurring",
                                "Ataxia",
                                "Spasticity",
                                "Numbness"
                        },

                        new MedicationTemplate[] {

                                new MedicationTemplate(
                                        "Baclofen",
                                        new String[] {
                                                "10mg",
                                                "20mg"
                                        },
                                        new String[] {
                                                "Three times daily"
                                        }
                                ),

                                new MedicationTemplate(
                                        "Gabapentin",
                                        new String[] {
                                                "100mg",
                                                "300mg"
                                        },
                                        new String[] {
                                                "Once daily",
                                                "Three times daily"
                                        }
                                )
                        }
                ),

                new DiagnosisTemplate(
                        "Tuberculosis",
                        "Infectious Disease",

                        new String[] {
                                "Pulmonary Tuberculosis",
                                "Latent Tuberculosis",
                                "Tuberculous Pleural Disease"
                        },

                        new String[] {
                                "Chronic Cough",
                                "Hemoptysis",
                                "Night Sweats",
                                "Fever",
                                "Weight Loss",
                                "Fatigue"
                        },

                        new MedicationTemplate[] {

                                new MedicationTemplate(
                                        "Isoniazid",
                                        new String[] {
                                                "300mg"
                                        },
                                        new String[] {
                                                "Once daily"
                                        }
                                ),

                                new MedicationTemplate(
                                        "Rifampicin",
                                        new String[] {
                                                "450mg",
                                                "600mg"
                                        },
                                        new String[] {
                                                "Once daily"
                                        }
                                )
                        }
                )
        };
    }

    // ============================================================
    // INPUT METHODS
    // ============================================================

    static String readLine(String message) {

        System.out.print(message);

        return scanner.nextLine().trim();
    }

    static int readInt(String message) {

        while (true) {

            try {

                System.out.print(message);

                return Integer.parseInt(
                        scanner.nextLine().trim()
                );

            } catch (Exception e) {

                System.out.println(
                        "Please enter a valid number."
                );
            }
        }
    }

    // ============================================================
    // CO1 - ALGORITHM SELECTION AND COMPLEXITY EVALUATION
    // Brief: Classify the problem, select suitable algorithms,
    // and compare their expected time-complexity characteristics.
    // ============================================================

    static void showCO1Analysis() {
        System.out.println("\n================ CO1: ALGORITHM SELECTION ================");
        System.out.println("Problem: Search medical terms and patient records efficiently.");
        System.out.println();
        System.out.println("Naive String Matching : worst-case O(n*m)");
        System.out.println("KMP String Matching   : O(n + m)");
        System.out.println("Rabin-Karp            : expected O(n + m), worst O(n*m)");
        System.out.println("Edit Distance / DP    : O(n*m)");
        System.out.println("Edmonds-Karp          : O(V*E^2)");
        System.out.println();
        System.out.println("Selection used:");
        System.out.println("- Exact text search -> string matching.");
        System.out.println("- Repeated pattern search -> KMP / Rabin-Karp.");
        System.out.println("- Typo correction -> Dynamic Programming / Edit Distance.");
        System.out.println("- Department capacity allocation -> Maximum Flow.");
        System.out.println("===========================================================");
    }

    // ============================================================
    // CO2 - STRING MATCHING: NAIVE, KMP, RABIN-KARP
    // Brief: Find every occurrence of a pattern inside medical text.
    // ============================================================

    static List<Integer> naiveSearch(String text, String pattern) {
        List<Integer> result = new ArrayList<>();

        if (text == null || pattern == null || pattern.length() == 0 ||
                pattern.length() > text.length()) {
            return result;
        }

        for (int i = 0; i <= text.length() - pattern.length(); i++) {
            int j = 0;

            while (j < pattern.length() &&
                    text.charAt(i + j) == pattern.charAt(j)) {
                j++;
            }

            if (j == pattern.length()) {
                result.add(i);
            }
        }

        return result;
    }

    // KMP failure/LPS function.
    static int[] buildLPS(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0;
        int i = 1;

        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                lps[i] = ++len;
                i++;
            } else if (len > 0) {
                len = lps[len - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }

        return lps;
    }

    static List<Integer> kmpSearch(String text, String pattern) {
        List<Integer> result = new ArrayList<>();

        if (text == null || pattern == null || pattern.length() == 0) {
            return result;
        }

        int[] lps = buildLPS(pattern);
        int i = 0;
        int j = 0;

        while (i < text.length()) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;

                if (j == pattern.length()) {
                    result.add(i - j);
                    j = lps[j - 1];
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }

        return result;
    }

    // Rabin-Karp uses a rolling hash to compare windows efficiently.
    static List<Integer> rabinKarpSearch(String text, String pattern) {
        List<Integer> result = new ArrayList<>();

        if (text == null || pattern == null || pattern.length() == 0 ||
                pattern.length() > text.length()) {
            return result;
        }

        final long BASE = 256;
        final long MOD = 1_000_000_007L;
        int m = pattern.length();

        long patternHash = 0;
        long windowHash = 0;
        long highPower = 1;

        for (int i = 0; i < m - 1; i++) {
            highPower = (highPower * BASE) % MOD;
        }

        for (int i = 0; i < m; i++) {
            patternHash =
                    (patternHash * BASE + pattern.charAt(i)) % MOD;

            windowHash =
                    (windowHash * BASE + text.charAt(i)) % MOD;
        }

        for (int i = 0; i <= text.length() - m; i++) {

            if (patternHash == windowHash) {
                boolean same = true;

                for (int j = 0; j < m; j++) {
                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        same = false;
                        break;
                    }
                }

                if (same) {
                    result.add(i);
                }
            }

            if (i < text.length() - m) {
                windowHash =
                        (windowHash -
                                text.charAt(i) * highPower) % MOD;

                if (windowHash < 0) {
                    windowHash += MOD;
                }

                windowHash =
                        (windowHash * BASE +
                                text.charAt(i + m)) % MOD;
            }
        }

        return result;
    }

    static void showCO2StringMatching() {
        System.out.println("\n================ CO2: STRING MATCHING ================");

        String text = readLine("Enter medical text: ").toLowerCase();
        String pattern =
                readLine("Enter pattern to search: ").toLowerCase();

        long start = System.nanoTime();
        List<Integer> naive = naiveSearch(text, pattern);
        long naiveTime = System.nanoTime() - start;

        start = System.nanoTime();
        List<Integer> kmp = kmpSearch(text, pattern);
        long kmpTime = System.nanoTime() - start;

        start = System.nanoTime();
        List<Integer> rabinKarp =
                rabinKarpSearch(text, pattern);
        long rabinKarpTime = System.nanoTime() - start;

        System.out.println("\nNaive matches     : " + naive);
        System.out.println("KMP matches       : " + kmp);
        System.out.println("Rabin-Karp matches: " + rabinKarp);

        System.out.println("\nNaive time        : " +
                naiveTime + " ns");
        System.out.println("KMP time          : " +
                kmpTime + " ns");
        System.out.println("Rabin-Karp time   : " +
                rabinKarpTime + " ns");

        System.out.println(
                "\nAll three algorithms should return the same positions."
        );

        System.out.println("======================================================");
    }

    // ============================================================
    // CO3 - DYNAMIC PROGRAMMING AND EDIT DISTANCE
    // Brief: Use overlapping subproblems and optimal substructure
    // to compute minimum insertions, deletions and substitutions.
    // ============================================================

    static void showCO3DynamicProgramming() {
        System.out.println(
                "\n================ CO3: DYNAMIC PROGRAMMING ================"
        );

        String first =
                readLine("Enter first word: ").toLowerCase();

        String second =
                readLine("Enter second word: ").toLowerCase();

        int distance =
                levenshteinDistance(first, second);

        System.out.println("\nEdit Distance = " + distance);

        System.out.println(
                "Minimum insertions, deletions, or substitutions: "
                        + distance
        );

        System.out.println("\nDP Matrix:");
        displayDPMatrix(first, second);

        System.out.println("===========================================================");
    }

    // ============================================================
    // CO4 - FLOW NETWORKS AND EDMONDS-KARP MAXIMUM FLOW
    // Brief: Model hospital department capacity as a flow network.
    // BFS finds augmenting paths in the residual graph.
    // ============================================================

    static int edmondsKarp(int[][] capacity, int source, int sink) {
        int n = capacity.length;
        int maxFlow = 0;

        while (true) {

            int[] parent = new int[n];
            Arrays.fill(parent, -1);
            parent[source] = source;

            int[] pathFlow = new int[n];
            pathFlow[source] = Integer.MAX_VALUE;

            Queue<Integer> queue = new ArrayDeque<>();
            queue.offer(source);

            while (!queue.isEmpty() && parent[sink] == -1) {

                int u = queue.poll();

                for (int v = 0; v < n; v++) {

                    if (parent[v] == -1 &&
                            capacity[u][v] > 0) {

                        parent[v] = u;

                        pathFlow[v] =
                                Math.min(
                                        pathFlow[u],
                                        capacity[u][v]
                                );

                        queue.offer(v);

                        if (v == sink) {
                            break;
                        }
                    }
                }
            }

            if (parent[sink] == -1) {
                break;
            }

            int flow = pathFlow[sink];
            maxFlow += flow;

            int v = sink;

            while (v != source) {
                int u = parent[v];

                capacity[u][v] -= flow;
                capacity[v][u] += flow;

                v = u;
            }
        }

        return maxFlow;
    }

    static void showCO4MaxFlow() {
        System.out.println(
                "\n================ CO4: HOSPITAL MAX FLOW ================"
        );

        System.out.println(
                "Hospital department capacity allocation using a flow network."
        );

        String[] departments = {
                "Cardiology",
                "Neurology",
                "Orthopedics",
                "General Medicine"
        };

        int[] capacities = {4, 3, 5, 6};

        int source = 0;
        int firstDepartment = 1;
        int sink = departments.length + 1;
        int n = sink + 1;

        int[][] graph = new int[n][n];

        for (int i = 0; i < departments.length; i++) {
            graph[source][firstDepartment + i] = capacities[i];
            graph[firstDepartment + i][sink] = capacities[i];
        }

        int requestedCases = 20;

        int[][] residual =
                new int[n][n];

        for (int i = 0; i < n; i++) {
            residual[i] =
                    Arrays.copyOf(graph[i], n);
        }

        int maxFlow =
                edmondsKarp(
                        residual,
                        source,
                        sink
                );

        int totalCapacity = 0;

        System.out.println("\nDepartment capacities:");

        for (int i = 0; i < departments.length; i++) {

            totalCapacity += capacities[i];

            System.out.println(
                    "- " + departments[i] +
                    " : " + capacities[i] + " cases"
            );
        }

        System.out.println("\nRequested cases : " +
                requestedCases);

        System.out.println("Total capacity  : " +
                totalCapacity);

        System.out.println("Maximum flow    : " +
                maxFlow);

        System.out.println(
                "Unallocated cases: " +
                Math.max(0, requestedCases - maxFlow)
        );

        System.out.println(
                "\nAlgorithm: Edmonds-Karp, a BFS-based implementation "
                        + "of Ford-Fulkerson."
        );

        System.out.println("========================================================");
    }

}