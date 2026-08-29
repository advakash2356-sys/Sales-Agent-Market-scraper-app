package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leads")
data class LeadEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val phone: String = "",
    val website: String = "",
    val googleMapsUrl: String = "",
    val queryNiche: String,
    val queryCity: String,
    val isDemo: Boolean = false,
    
    // Phase 2: Audit fields (Nullable if not yet audited)
    val pageSpeedScore: Int? = null, 
    val speedLoadTime: String = "N/A", 
    val hasSsl: Boolean = false,
    val isMobileFriendly: Boolean = false,
    val hasGoogleMyBusinessActive: Boolean = false,
    val hasContactForm: Boolean = false,
    val hasSocialMediaLinked: Boolean = false,
    val biggestGapOneLiner: String = "No audit run yet.",
    val estimatedLostRevenue: Int = 0, 

    // Phase 3: Rank fields
    val conversionScore: Int = 50,
    val notes: String = "",

    // Phase 4: Generated Lovable/Bolt Prompt
    val generatedPrompt: String = "",

    // Phase 5: Outreach templates
    val emailSubject: String = "",
    val emailBodyEnglish: String = "",
    val emailBodyHinglish: String = "",
    val whatsappEnglish: String = "",
    val whatsappHinglish: String = "",
    val followUpEnglish: String = "",
    val followUpHinglish: String = "",

    // Real-time Google verification status
    val isGoogleVerified: Boolean = false,
    val googleVerificationStatus: String = "Unverified", // "Verified Match", "Verified Flagged (Mismatch)", "Verification Failed (Not Found)"
    val googleVerificationNotes: String = "Google search verification has not been initiated.",
    val googleConfidenceScore: Int = 0,
    val verifiedPhone: String = "",
    val verifiedWebsite: String = ""
)
