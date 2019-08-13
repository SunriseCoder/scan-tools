package process.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "languages")
public class LanguageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_two_letters")
    private String nameTwoLetters;

    @Column(name = "name_three_letters")
    private String nameThreeLetters;

    @Column(name = "name_in_english")
    private String nameInEnglish;

    @Column(name = "name_in_language")
    private String nameInLanguage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNameTwoLetters() {
        return nameTwoLetters;
    }

    public void setNameTwoLetters(String nameTwoLetters) {
        this.nameTwoLetters = nameTwoLetters;
    }

    public String getNameThreeLetters() {
        return nameThreeLetters;
    }

    public void setNameThreeLetters(String nameThreeLetters) {
        this.nameThreeLetters = nameThreeLetters;
    }

    public String getNameInEnglish() {
        return nameInEnglish;
    }

    public void setNameInEnglish(String nameInEnglish) {
        this.nameInEnglish = nameInEnglish;
    }

    public String getNameInLanguage() {
        return nameInLanguage;
    }

    public void setNameInLanguage(String nameInLanguage) {
        this.nameInLanguage = nameInLanguage;
    }
}
