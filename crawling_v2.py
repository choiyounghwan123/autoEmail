import os.path
from urllib.parse import urljoin
import bs4
import json
import requests
from bs4 import BeautifulSoup
import transformers
import torch
import os

TITLE_FILE_PATH = "titles.json"


def save_titles(titles):
    with open(TITLE_FILE_PATH, 'w', encoding='utf-8') as f:
        json.dump(titles, f, ensure_ascii=False, indent=4)


def load_saved_titles():
    if os.path.exists(TITLE_FILE_PATH):
        with open(TITLE_FILE_PATH, 'r', encoding='utf-8') as f:
            return json.load(f)
        return []


def get_post(URL: str) -> list:
    try:
        response = requests.get(URL)
        response.raise_for_status()
    except requests.RequestException as e:
        print(f"get_post error 발생: {e}")

    soup = BeautifulSoup(response.text, "html.parser")

    return soup.find('tbody').find_all('tr', class_=lambda x: x != "headline")


def get_post_details(post: bs4.element.Tag) -> dict:
    url = "https://bce.pusan.ac.kr/"
    title = post.find('strong').get_text(strip=True)
    NOTICE_LINK = url + post.find('a', class_="artclLinkView")['href']

    try:
        notice_response = requests.get(NOTICE_LINK)
        notice_response.raise_for_status()
    except requests.RequestException as e:
        print(f"get_post_details error 발생: {e}")

    notice_soup = BeautifulSoup(notice_response.text, 'html.parser')
    content = notice_soup.find('div', class_="artclView").get_text()
    images = notice_soup.find_all("img")

    image_urls = [urljoin(url, img['src']) for img in images]

    return {"title": title, "URL": NOTICE_LINK, "content": content, "images": image_urls}


def save_image(image_url: str):
    img_name = os.path.basename(image_url)

    try:
        img_response = requests.get(image_url)
        img_response.raise_for_status()
    except requests.RequestException as e:
        print(f"error 발생: {e}")
        return

    os.makedirs('images', exist_ok=True)
    img_path = os.path.join('images', img_name)

    with open(img_path, 'wb') as f:
        f.write(img_response.content)
    print(f"Image saved: {img_path}")
    return img_path


def main():
    URL = "https://bce.pusan.ac.kr/bce/50189/subview.do"
    posts = get_post(URL)
    summarize_and_send(posts)


def send_to_server(data, image_paths):
    server_url = "http://localhost:8081/api/emails/send"

    # ContentDto는 JSON 형태로 전송해야 함
    files = {
        'content': (None, json.dumps(data), 'application/json'),  # JSON 데이터를 multipart로 전송
    }

    # 이미지 파일을 추가
    for i, image_path in enumerate(image_paths):
        if os.path.exists(image_path):
            files[f'images'] = (os.path.basename(image_path), open(image_path, 'rb'), 'image/jpeg')

    try:
        # multipart/form-data로 content와 이미지 함께 전송
        response = requests.post(server_url, files=files)
        response.raise_for_status()
        print("Data successfully sent to server.")
    except requests.RequestException as e:
        print(f"Failed to send data to server: {e}")
    finally:
        # 열린 파일들 닫기
        for key, file_info in files.items():
            if file_info[1] and not isinstance(file_info[1], str):  # 열린 파일일 경우
                file_info[1].close()


def summarize_and_send(posts):
    model_id = "MLP-KTLim/llama-3-Korean-Bllossom-8B"

    pipeline = transformers.pipeline(
        "text-generation",
        model=model_id,
        model_kwargs={"torch_dtype": torch.float32},
        device_map="auto",
    )

    pipeline.model.eval()

    saved_titles = load_saved_titles()

    summarized_data = []  # 요약된 데이터 저장 리스트
    PROMPT = '''You are a helpful AI assistant. Please answer the user's questions kindly. 당신은 유능한 AI 어시스턴트 입니다. 사용자의 질문에 대해 친절하게 답변해주세요.'''

    current_titles = []

    for post in posts:
        img_paths = []
        post_detail = get_post_details(post)

        if post_detail['title'] in saved_titles:
            print("continue")
            current_titles.append(post_detail['title'])
            continue
        else:
            print("save")
            current_titles.append(post_detail['title'])

        if post_detail['images']:
            for image in post_detail['images']:
                img_paths.append(save_image(image))

        instruction = post_detail['content'] + "다른말 없이 해당 내용만 요약해줘"
        print(post_detail['content'])
        if post_detail['content'] == "":
            summarized_data = {
                "title": post_detail['title'],
                "URL": post_detail['URL'],
                "content": "본문 없음"
            }
            print(summarized_data)
            # 요약된 데이터를 서버로 전송
            send_to_server(summarized_data, img_paths)
            return
        messages = [{"role": "system", "content": f"{PROMPT}"},
                    {"role": "user", "content": f"{instruction}"}]

        prompt = pipeline.tokenizer.apply_chat_template(
            messages,
            tokenize=False,
            add_generation_prompt=True
        )

        terminators = [
            pipeline.tokenizer.eos_token_id,
            pipeline.tokenizer.convert_tokens_to_ids("<|eot_id|>")
        ]

        outputs = pipeline(
            prompt,
            max_new_tokens=2048,
            eos_token_id=terminators,
            do_sample=True,
            temperature=0.6,
            top_p=0.9
        )

        summarized_content = outputs[0]["generated_text"][len(prompt):].strip()

        # 요약된 결과를 title, url과 함께 저장
        summarized_data = {
            "title": post_detail['title'],
            "URL": post_detail['URL'],
            "content": summarized_content
        }

        print(summarized_data)
        # 요약된 데이터를 서버로 전송
        send_to_server(summarized_data, img_paths)
    save_titles(current_titles)


if __name__ == "__main__":
    main()
