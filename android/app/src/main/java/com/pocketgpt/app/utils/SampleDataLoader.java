package com.pocketgpt.app.utils;

import android.content.Context;
import com.pocketgpt.app.model.AppDocument;
import com.pocketgpt.app.model.DocumentChunk;
import com.pocketgpt.app.repository.SearchDao;
import com.pocketgpt.app.services.EmbeddingService;

import java.util.ArrayList;
import java.util.List;

public class SampleDataLoader {

    public interface LoadCallback {
        void onLoaded(int docCount, int chunkCount);
    }

    public static void loadSampleDocuments(Context context, LoadCallback callback) {
        new Thread(() -> {
            SearchDao dao = PocketGptDatabase.getDatabase(context).searchDao();
            EmbeddingService embeddingService = EmbeddingService.create();

            List<AppDocument> sampleDocs = createSampleDocuments();
            int totalChunks = 0;

            for (AppDocument doc : sampleDocs) {
                // Check if already exists
                AppDocument existing = dao.getDocumentByTitle(doc.title);
                if (existing != null) {
                    continue;
                }

                // Insert AppDocument
                long insertedId = dao.insertDocument(doc);
                int docId = (int) insertedId;

                // Chunk the document
                List<String> chunkTexts = DocumentChunker.chunkText(doc.content, 400, 40);
                List<DocumentChunk> chunks = new ArrayList<>();

                for (int i = 0; i < chunkTexts.size(); i++) {
                    String chunkText = chunkTexts.get(i);
                    float[] vector = embeddingService.generateEmbedding(chunkText);
                    String vectorStr = embeddingService.serializeVector(vector);

                    chunks.add(new DocumentChunk(docId, doc.title, chunkText, i, vectorStr));
                }

                if (!chunks.isEmpty()) {
                    dao.insertChunks(chunks);
                    doc.id = docId;
                    doc.chunkCount = chunks.size();
                    dao.updateDocument(doc);
                    totalChunks += chunks.size();
                }
            }

            int finalDocCount = dao.getDocumentCount();
            int finalChunkCount = dao.getChunkCount();
            if (callback != null) {
                callback.onLoaded(finalDocCount, finalChunkCount);
            }
        }).start();
    }

    public static List<AppDocument> createSampleDocuments() {
        List<AppDocument> list = new ArrayList<>();

        // 1. California Consumer Privacy Act (CCPA)
        String ccpaContent = "CALIFORNIA CONSUMER PRIVACY ACT (CCPA) & CPRA FRAMEWORK\n\n" +
                "1. Fundamental Consumer Rights:\n" +
                "- Right to Know: Consumers have the right to request disclosure of what personal information a business collects, uses, discloses, and sells about them.\n" +
                "- Right to Delete: Consumers can request the deletion of their personal information collected by businesses, subject to certain legal exceptions (e.g., fulfilling contracts, compliance with legal obligations).\n" +
                "- Right to Opt-Out of Sale / Sharing: Businesses must provide a clear 'Do Not Sell or Share My Personal Info' link on their homepage. Consumers can opt out at any time.\n" +
                "- Right to Correct: Consumers can request correction of inaccurate personal information held by businesses.\n" +
                "- Right to Limit Use of Sensitive Personal Info: Consumers can limit the use of SSNs, financial details, precise geolocation, and health data.\n" +
                "- Right to Non-Discrimination: Businesses cannot deny goods, charge different prices, or provide different quality of services to consumers exercising their privacy rights.\n\n" +
                "2. Business Applicability Thresholds:\n" +
                "Applies to for-profit businesses operating in California that meet one or more criteria:\n" +
                "- Annual gross revenue exceeding $25 million.\n" +
                "- Buys, sells, or shares the personal info of 100,000 or more consumers or households.\n" +
                "- Derives 50% or more of its annual revenues from selling consumer personal information.\n\n" +
                "3. Enforcement and Fines:\n" +
                "Enforced by the California Privacy Protection Agency (CPPA) and California Attorney General. Fines up to $2,500 per unintentional violation and $7,500 per intentional violation or violations involving minors.";
        list.add(new AppDocument("California Consumer Privacy Act (CCPA)", ccpaContent, "PRIVACY_ACT", "Effective 2020 / Am. 2023"));

        // 2. GDPR
        String gdprContent = "GENERAL DATA PROTECTION REGULATION (GDPR) - EU PRIVACY OVERVIEW\n\n" +
                "1. Core Principles of Data Protection:\n" +
                "- Lawfulness, fairness, and transparency: Data must be processed legally and openly.\n" +
                "- Purpose limitation: Data collected only for specified, explicit, and legitimate purposes.\n" +
                "- Data minimization: Data collected must be adequate, relevant, and limited to what is strictly necessary.\n" +
                "- Accuracy: Personal data must be accurate and kept up to date.\n" +
                "- Storage limitation: Data kept in identifiable form no longer than necessary.\n" +
                "- Integrity and confidentiality (Security): Protection against unauthorized processing, loss, or damage.\n\n" +
                "2. Key Data Subject Rights:\n" +
                "- Right of access (Article 15)\n" +
                "- Right to rectification (Article 16)\n" +
                "- Right to erasure / Right to be forgotten (Article 17)\n" +
                "- Right to restriction of processing (Article 18)\n" +
                "- Right to data portability (Article 20)\n" +
                "- Right to object to automated decision-making and profiling (Article 21 & 22)\n\n" +
                "3. Breach Notification Mandate:\n" +
                "Data controllers must notify the supervisory authority within 72 hours of becoming aware of a personal data breach posing risk to individuals.\n\n" +
                "4. Penalties:\n" +
                "Tier 1: Up to €10 million or 2% of worldwide annual turnover for administrative infringements.\n" +
                "Tier 2: Up to €20 million or 4% of worldwide annual turnover for core principle violations.";
        list.add(new AppDocument("GDPR Compliance & Data Protection Regulation", gdprContent, "REGULATION", "Effective May 2018"));

        // 3. Indian Constitution
        String constContent = "THE CONSTITUTION OF INDIA - PART III: FUNDAMENTAL RIGHTS\n\n" +
                "1. Right to Equality (Articles 14 - 18):\n" +
                "- Article 14: Equality before law and equal protection of the laws within the territory of India.\n" +
                "- Article 15: Prohibition of discrimination on grounds of religion, race, caste, sex, or place of birth.\n" +
                "- Article 16: Equality of opportunity in matters of public employment.\n" +
                "- Article 17: Abolition of Untouchability and prohibition of its practice.\n" +
                "- Article 18: Abolition of all titles except military and academic distinctions.\n\n" +
                "2. Right to Freedom (Articles 19 - 22):\n" +
                "- Article 19: Protection of six freedoms: Speech & expression, peaceful assembly, forming associations/unions, free movement across India, residence anywhere in India, practicing any profession or trade.\n" +
                "- Article 20: Protection in respect of conviction for offences (no ex-post facto law, double jeopardy, self-incrimination).\n" +
                "- Article 21: Protection of life and personal liberty. Includes Right to Privacy (Puttaswamy judgment).\n" +
                "- Article 21A: Right to free and compulsory education for children aged 6 to 14 years.\n" +
                "- Article 22: Protection against arrest and detention in certain cases.\n\n" +
                "3. Right to Constitutional Remedies (Article 32):\n" +
                "Right to move the Supreme Court of India for the enforcement of Fundamental Rights by issuance of Writs (Habeas Corpus, Mandamus, Prohibition, Quo-Warranto, Certiorari). Termed by Dr. B.R. Ambedkar as the 'Heart and Soul of the Constitution'.";
        list.add(new AppDocument("Constitution of India - Fundamental Rights", constContent, "CONSTITUTION", "Enacted Jan 1950"));

        // 4. EU AI Act
        String aiActContent = "EUROPEAN UNION ARTIFICIAL INTELLIGENCE ACT (EU AI ACT)\n\n" +
                "1. Risk-Based Classification System:\n" +
                "- Unacceptable Risk (Prohibited): AI systems deploying subliminal manipulation, social scoring, predictive policing based on profiling, and untargeted scraping of facial images from the internet or CCTV.\n" +
                "- High Risk (Regulated): AI in critical infrastructure, medical devices, educational assessment, biometric identification, employment recruitment, credit scoring, and law enforcement. Requires strict risk assessment, high quality training data, logging, human oversight, and conformity assessments.\n" +
                "- Limited Risk (Transparency obligations): Chatbots and generative AI must clearly disclose that the user is interacting with an AI system. Deepfakes and AI-generated text/media must be watermarked.\n" +
                "- Minimal / Zero Risk (Unrestricted): Spam filters, AI-enabled video games, and general business tools.\n\n" +
                "2. General Purpose AI (GPAI) Models:\n" +
                "Providers of foundation models must adhere to transparency obligations, copyright compliance, and technical summaries. Systemic GPAI models face additional adversarial testing and incident reporting.\n\n" +
                "3. Penalties for Non-Compliance:\n" +
                "Violations of prohibited AI practices carry fines up to €35 million or 7% of worldwide annual turnover, whichever is higher.";
        list.add(new AppDocument("EU Artificial Intelligence Act (AI Act)", aiActContent, "LEGISLATION", "Passed 2024"));

        return list;
    }
}

