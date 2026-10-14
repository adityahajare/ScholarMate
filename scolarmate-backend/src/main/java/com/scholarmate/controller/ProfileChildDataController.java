package com.scholarmate.controller;

import com.scholarmate.dto.*;
import com.scholarmate.entity.*;
import com.scholarmate.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileChildDataController {
    private final UserProfileRepository profiles;
    private final FamilyMemberRepository families;
    private final ProfileAddressRepository addresses;
    private final ProfileDocumentRepository documents;
    private final com.scholarmate.service.ProfileDocumentStorageService documentStorage;

    public ProfileChildDataController(UserProfileRepository profiles, FamilyMemberRepository families,
                                      ProfileAddressRepository addresses, ProfileDocumentRepository documents,
                                      com.scholarmate.service.ProfileDocumentStorageService documentStorage) {
        this.profiles = profiles; this.families = families; this.addresses = addresses; this.documents = documents; this.documentStorage = documentStorage;
    }

    @GetMapping("/family") public List<FamilyMember> families(Authentication a) { return families.findByUserProfileIdOrderByIdAsc(profile(a).getId()); }
    @PostMapping("/family") @Transactional public FamilyMember addFamily(Authentication a, @RequestBody FamilyMemberRequest r) {
        require(r.relationship(), "relationship"); require(r.name(), "name");
        return families.save(FamilyMember.builder().userProfile(profile(a)).relationship(r.relationship()).name(r.name()).occupation(r.occupation()).annualIncome(r.annualIncome()).dependent(r.dependent()).beneficiaryCount(r.beneficiaryCount()).build());
    }
    @PutMapping("/family/{id}") @Transactional public FamilyMember updateFamily(Authentication a, @PathVariable Long id, @RequestBody FamilyMemberRequest r) {
        FamilyMember e = families.findByIdAndUserProfileId(id, profile(a).getId()).orElseThrow(() -> new IllegalArgumentException("Family member not found"));
        e.setRelationship(r.relationship()); e.setName(r.name()); e.setOccupation(r.occupation()); e.setAnnualIncome(r.annualIncome()); e.setDependent(r.dependent()); e.setBeneficiaryCount(r.beneficiaryCount()); return e;
    }
    @DeleteMapping("/family/{id}") public ResponseEntity<Void> deleteFamily(Authentication a, @PathVariable Long id) { families.delete(families.findByIdAndUserProfileId(id, profile(a).getId()).orElseThrow(() -> new IllegalArgumentException("Family member not found"))); return ResponseEntity.noContent().build(); }

    @GetMapping("/addresses") public List<ProfileAddress> addresses(Authentication a) { return addresses.findByUserProfileIdOrderByIdAsc(profile(a).getId()); }
    @PostMapping("/addresses") @Transactional public ProfileAddress addAddress(Authentication a, @RequestBody ProfileAddressRequest r) {
        require(r.addressType(), "addressType"); return addresses.save(ProfileAddress.builder().userProfile(profile(a)).addressType(r.addressType()).line1(r.line1()).line2(r.line2()).state(r.state()).district(r.district()).taluka(r.taluka()).village(r.village()).pinCode(r.pinCode()).domicile(r.domicile()).build());
    }
    @PutMapping("/addresses/{id}") @Transactional public ProfileAddress updateAddress(Authentication a, @PathVariable Long id, @RequestBody ProfileAddressRequest r) {
        ProfileAddress e = addresses.findByIdAndUserProfileId(id, profile(a).getId()).orElseThrow(() -> new IllegalArgumentException("Address not found"));
        e.setAddressType(r.addressType()); e.setLine1(r.line1()); e.setLine2(r.line2()); e.setState(r.state()); e.setDistrict(r.district()); e.setTaluka(r.taluka()); e.setVillage(r.village()); e.setPinCode(r.pinCode()); e.setDomicile(r.domicile()); return e;
    }
    @DeleteMapping("/addresses/{id}") public ResponseEntity<Void> deleteAddress(Authentication a, @PathVariable Long id) { addresses.delete(addresses.findByIdAndUserProfileId(id, profile(a).getId()).orElseThrow(() -> new IllegalArgumentException("Address not found"))); return ResponseEntity.noContent().build(); }

    @GetMapping("/documents") public List<ProfileDocument> documents(Authentication a) { return documents.findByUserProfileIdOrderByUploadedAtDesc(profile(a).getId()); }
    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional public ProfileDocument uploadDocument(Authentication a, @RequestParam String documentType, @RequestPart("file") MultipartFile file) {
        require(documentType, "documentType");
        return documentStorage.store(profile(a), documentType, file);
    }
    @GetMapping("/documents/{id}/download") public ResponseEntity<Resource> downloadDocument(Authentication a, @PathVariable Long id) {
        ProfileDocument document = documents.findByIdAndUserProfileId(id, profile(a).getId()).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        Resource resource = documentStorage.load(document);
        BodyBuilder response = ResponseEntity.ok();
        if (document.getContentType() != null) response.contentType(MediaType.parseMediaType(document.getContentType()));
        return response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"").body(resource);
    }
    @PostMapping("/documents") @Transactional public ProfileDocument addDocument(Authentication a, @RequestBody ProfileDocumentRequest r) {
        require(r.documentType(), "documentType"); require(r.fileName(), "fileName"); validateSize(r.sizeBytes());
        ProfileDocument d = ProfileDocument.builder().userProfile(profile(a)).documentType(r.documentType()).fileName(r.fileName()).contentType(r.contentType()).sizeBytes(r.sizeBytes()).storageKey(r.storageKey()).status(normalizeStatus(r.status())).extractedData(r.extractedData()).expiresAt(parseDate(r.expiresAt())).build();
        return documents.save(d);
    }
    @PutMapping("/documents/{id}") @Transactional public ProfileDocument updateDocument(Authentication a, @PathVariable Long id, @RequestBody ProfileDocumentRequest r) {
        ProfileDocument d = documents.findByIdAndUserProfileId(id, profile(a).getId()).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (r.status() != null) { d.setStatus(normalizeStatus(r.status())); if ("STUDENT_CONFIRMED".equals(d.getStatus())) d.setConfirmedAt(LocalDateTime.now()); if ("VERIFIED".equals(d.getStatus())) d.setVerifiedAt(LocalDateTime.now()); }
        if (r.extractedData() != null) d.setExtractedData(r.extractedData()); if (r.expiresAt() != null) d.setExpiresAt(parseDate(r.expiresAt())); return d;
    }
    @DeleteMapping("/documents/{id}") @Transactional public ResponseEntity<Void> deleteDocument(Authentication a, @PathVariable Long id) { ProfileDocument document = documents.findByIdAndUserProfileId(id, profile(a).getId()).orElseThrow(() -> new IllegalArgumentException("Document not found")); documentStorage.deleteFile(document); documents.delete(document); return ResponseEntity.noContent().build(); }

    private UserProfile profile(Authentication a) { return profiles.findByUserEmail(a.getName()).orElseThrow(() -> new IllegalArgumentException("Profile not found")); }
    private static void require(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required"); }
    private static void validateSize(Long size) { if (size != null && (size < 0 || size > 10_485_760L)) throw new IllegalArgumentException("Document must be at most 10 MB"); }
    private static String normalizeStatus(String status) { String s = status == null ? "UPLOADED" : status.toUpperCase(); if (!List.of("MISSING","UPLOADED","OCR_EXTRACTED","STUDENT_CONFIRMED","VERIFIED","REJECTED","EXPIRED").contains(s)) throw new IllegalArgumentException("Invalid document status"); return s; }
    private static LocalDate parseDate(String date) { return date == null || date.isBlank() ? null : LocalDate.parse(date); }
}
